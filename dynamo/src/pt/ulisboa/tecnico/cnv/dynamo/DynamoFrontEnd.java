package pt.ulisboa.tecnico.cnv.dynamo;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import metrics.tools.Stats;
import metrics.tools.StatsBFS;
import metrics.tools.StatsCP;
import metrics.tools.StatsDLX;
import pt.ulisboa.tecnico.cnv.dynamo.cache.LRUCache;
import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuParameters;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamoFrontEnd {
    private static final String REGION = "us-east-1";
    private static final String KEY_REQUEST_PRIMARY_KEY = "RequestQuery";
    private static final String KEY_BOARD_SIZE_N1 = "BoardSizeN1";
    private static final String KEY_BOARD_SIZE_N2 = "BoardSizeN2";
    private static final String KEY_UNASSIGNED_ENTRIES = "UnassignedEntries";
    private static final String KEY_SHORT_KEY = KEY_UNASSIGNED_ENTRIES;
    private static final String KEY_BOARD_IDENTIFIER = "BoardIdentifier";
    private static final String KEY_REQUEST_COST = "RequestCost";
    private static final int CACHE_CAPACITY = 200;
    private static final int SCAN_RESULT_LIMIT = 1;
    private static final int UN_ACCEPTABLE_INTERVAL = 20;
    private static final long TABLE_READ_CAPACITY = 5;
    private static final long TABLE_WRITE_CAPACITY = 5;

    private static final long COST_9x9_BFS = (long) (4 * Math.pow(10, 8));
    private static final long COST_9x9_CP = (long) (3 * Math.pow(10, 8));
    private static final long COST_9x9_DLX = (long) (5 * Math.pow(10, 8));
    private static final long COST_16x16_BFS = (long) (2.7 * Math.pow(10, 9));
    private static final long COST_16x16_CP = (long) (2.5 * Math.pow(10, 9));
    private static final long COST_16x16_DLX = (long) (1.9 * Math.pow(10, 9));
    private static final long COST_25x25_BFS = (long) (3.9 * Math.pow(10, 9));
    private static final long COST_25x25_CP = (long) (Math.pow(10, 10));
    private static final long COST_25x25_DLX = (long) (5.6 * Math.pow(10, 9));

    private static Map<String, Long> requestCostCache = Collections.synchronizedMap(new LRUCache<String, Long>(CACHE_CAPACITY));
    private static AmazonDynamoDB dynamoDB;

    static {
        //Start dynamo DB on class load.
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        dynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(REGION)
                .build();
    }

    public static void createTables() {
        try {
            createStatsTable(StatsBFS.BFS_TABLE_NAME);
            createStatsTable(StatsCP.CP_TABLE_NAME);
            createStatsTable(StatsDLX.DLX_TABLE_NAME);

            TableUtils.waitUntilActive(dynamoDB, StatsBFS.BFS_TABLE_NAME);
            TableUtils.waitUntilActive(dynamoDB, StatsCP.CP_TABLE_NAME);
            TableUtils.waitUntilActive(dynamoDB, StatsDLX.DLX_TABLE_NAME);

        } catch (InterruptedException e) {
            System.out.println("ERROR: was interrupted while creating tables");
            System.exit(1);
        }
    }

    public static void uploadStats(SolverArgumentParser parser, Stats stats) {
        Map<String, AttributeValue> item = new HashMap<>();
        String key = getKey(parser);
        item.put(KEY_REQUEST_PRIMARY_KEY, new AttributeValue(key));
        item.put(KEY_BOARD_SIZE_N1, new AttributeValue().withN(Integer.toString(parser.getN1())));
        item.put(KEY_BOARD_SIZE_N2, new AttributeValue().withN(Integer.toString(parser.getN2())));
        item.put(KEY_BOARD_IDENTIFIER, new AttributeValue(parser.getInputBoard()));
        item.put(KEY_UNASSIGNED_ENTRIES, new AttributeValue().withN(Integer.toString(parser.getUn())));
        item.put(KEY_REQUEST_COST, new AttributeValue().withN(Long.toString(stats.getCost())));
        PutItemRequest putItemRequest = new PutItemRequest(stats.getTableName(), item)
                .withConditionExpression(String.format("attribute_not_exists(%s)", KEY_REQUEST_PRIMARY_KEY));
        try {
            dynamoDB.putItem(putItemRequest);
        } catch (ConditionalCheckFailedException e) {
            //Item with that key already exists
            System.out.println("Key already existed: " + key);
        }
    }

    public static long inferCost(SudokuParameters parameters) {
        Long cost = inferCostFilterBoard(parameters);
        if (cost != null) {
            return cost;
        } else {
            cost = inferCostAnyBoard(parameters);
            if (cost != null) {
                return cost;
            } else {
                return unknownFixedCost(parameters);
            }
        }
    }

    private static Long inferCostFilterBoard(SudokuParameters parameters) {
        String cacheKey = getCacheKey(parameters);
        Long value = getCostFromCache(cacheKey);
        return value != null ? value : filterBoardQuery(parameters);
    }

    private static Long inferCostAnyBoard(SudokuParameters parameters) {
        return costQuery(anyBoardFilter(parameters), parameters.getTableName());
    }

    private static Long getCostFromCache(String cacheKey) {
        return requestCostCache.get(cacheKey);
    }

    private static Long filterBoardQuery(SudokuParameters parameters) {
        Long value = costQuery(filterBoardFilter(parameters), parameters.getTableName());
        if (value != null) {
            requestCostCache.put(getCacheKey(parameters), value);
        }
        return value;
    }

    private static HashMap<String, Condition> filterBoardFilter(SudokuParameters parameters) {
        return parameterFilter(parameters, true);
    }

    private static HashMap<String, Condition> anyBoardFilter(SudokuParameters parameters) {
        return parameterFilter(parameters, false);
    }

    private static HashMap<String, Condition> parameterFilter(SudokuParameters parameters, boolean filterBoard) {
        HashMap<String, Condition> scanFilter = new HashMap<>();

        Condition equalN1Condition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withN(Integer.toString(parameters.getN1())));
        scanFilter.put(KEY_BOARD_SIZE_N1, equalN1Condition);

        Condition equalN2Condition = new Condition()
                .withComparisonOperator(ComparisonOperator.EQ.toString())
                .withAttributeValueList(new AttributeValue().withN(Integer.toString(parameters.getN2())));
        scanFilter.put(KEY_BOARD_SIZE_N2, equalN2Condition);

        if (filterBoard) {
            Condition equalBoardCondition = new Condition()
                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                    .withAttributeValueList(new AttributeValue(parameters.getInputBoard()));
            scanFilter.put(KEY_BOARD_IDENTIFIER, equalBoardCondition);
        }

        Condition unBetweenCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.BETWEEN.toString())
                .withAttributeValueList(new AttributeValue().withN(Integer.toString(parameters.getUn())),
                        new AttributeValue().withN(Integer.toString(parameters.getUn() + UN_ACCEPTABLE_INTERVAL)));

        scanFilter.put(KEY_UNASSIGNED_ENTRIES, unBetweenCondition);

        return scanFilter;
    }

    private static Long costQuery(HashMap<String, Condition> scanFilter, String tableName) {
        ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
        ScanResult scanResult = dynamoDB.scan(scanRequest).withCount(SCAN_RESULT_LIMIT);
        List<Map<String, AttributeValue>> items = scanResult.getItems();
        return items.size() == 0 ? null : Long.parseLong(items.get(0).get(KEY_REQUEST_COST).getN());
    }

    private static long unknownFixedCost(SudokuParameters parameters) {
        switch (parameters.getStrategy()) {
            case BFS:
                return unknownFixedCostBFS(parameters);
            case CP:
                return unknownFixedCostCP(parameters);
            case DLX:
            default: //Should never be default
                return unknownFixedCostDLX(parameters);
        }
    }

    private static long unknownFixedCostBFS(SudokuParameters parameters) {
        switch (parameters.getN1()) {
            case 9:
                return COST_9x9_BFS;
            case 16:
                return COST_16x16_BFS;
            case 25:
            default: //Should never be default
                return COST_25x25_BFS;
        }
    }

    private static long unknownFixedCostCP(SudokuParameters parameters) {
        switch (parameters.getN1()) {
            case 9:
                return COST_9x9_CP;
            case 16:
                return COST_16x16_CP;
            case 25:
            default: //Should never be default
                return COST_25x25_CP;
        }
    }

    private static long unknownFixedCostDLX(SudokuParameters parameters) {
        switch (parameters.getN1()) {
            case 9:
                return COST_9x9_DLX;
            case 16:
                return COST_16x16_DLX;
            case 25:
            default: //Should never be default
                return COST_25x25_DLX;
        }
    }



    /**
     * Stats tables: Primary key is unique combination of board and unassigned entrie
     * Sort key is unassigned entries
     */
    private static void createStatsTable(String name) {
        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(name)
                .withKeySchema(new KeySchemaElement().withAttributeName(KEY_REQUEST_PRIMARY_KEY).withKeyType(KeyType.HASH),
                        new KeySchemaElement().withAttributeName(KEY_SHORT_KEY).withKeyType(KeyType.RANGE))
                .withAttributeDefinitions(new AttributeDefinition().withAttributeName(KEY_REQUEST_PRIMARY_KEY).withAttributeType(ScalarAttributeType.S),
                        new AttributeDefinition().withAttributeName(KEY_SHORT_KEY).withAttributeType(ScalarAttributeType.N))
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(TABLE_READ_CAPACITY).withWriteCapacityUnits(TABLE_WRITE_CAPACITY));

        TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
    }

    private static String getKey(SolverArgumentParser parser) {
        return String.format("N1:%d&N2:%d&UN:%d&BOARD:%s", parser.getN1(), parser.getN2(), parser.getUn(), parser.getInputBoard());
    }


    private static String getKey(SudokuParameters parameters) {
        return String.format("N1:%d&N2:%d&UN:%d&BOARD:%s", parameters.getN1(), parameters.getN2(), parameters.getUn(), parameters.getInputBoard());
    }

    private static String getCacheKey(SudokuParameters parameters) {
        return getKey(parameters) + parameters.getTableName();
    }

    //Prevent utility class initialization
    private DynamoFrontEnd() {
    }
}
