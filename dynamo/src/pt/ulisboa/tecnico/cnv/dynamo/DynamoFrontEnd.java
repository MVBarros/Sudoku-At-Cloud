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
    private static final double BFS_N1_SLOPE = -298.2783986;
    private static final double BFS_N2_SLOPE = -298.2783986;
    private static final double BFS_UN_SLOPE = 927.19685145;;
    private static final double CP_N1_SLOPE = -1905.43807419;
    private static final double CP_N2_SLOPE = -1905.43807419;
    private static final double CP_UN_SLOPE = 1641.30725146;
    private static final double DLX_N1_SLOPE = 8002346.72978024;
    private static final double DLX_N2_SLOPE = 8002346.72978024;
    private static final double DLX_UN_SLOPE = 1344284.95812563;

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
        return (long) Math.abs(BFS_N1_SLOPE * parameters.getN1() + BFS_N2_SLOPE * parameters.getN2() + BFS_UN_SLOPE * parameters.getUn());
    }

    private static long unknownFixedCostCP(SudokuParameters parameters) {
        return (long) Math.abs(CP_N1_SLOPE * parameters.getN1() + CP_N2_SLOPE * parameters.getN2() + CP_UN_SLOPE * parameters.getUn());
    }

    private static long unknownFixedCostDLX(SudokuParameters parameters) {
        return (long) Math.abs(DLX_N1_SLOPE * parameters.getN1() + DLX_N2_SLOPE * parameters.getN2() + DLX_UN_SLOPE * parameters.getUn());
    }

    /**
     * Stats tables: Primary key is unique combination of board and unassigned entry
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
