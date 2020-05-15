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
    private static final String KEY_BOARD_IDENTIFIER = "BoardIdentifier";
    private static final String KEY_REQUEST_COST = "RequestCost";
    private static final int CACHE_CAPACITY = 200;

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

    //FIXME Do the full getCost, this for now is just for testing
    public static long getCost(SudokuParameters parameters) {
        String key = getKey(parameters);
        Long cost = getExactCost(key, parameters.getTableName());
        if (cost != null) {
            return cost;
        } else {
            //TODO REST OF QUERIES
            return 0;
        }
    }

    private static Long getExactCost(String key, String tableName) {
        String cacheKey = getCacheKey(key, tableName);
        Long value = requestCostCache.get(cacheKey);
        if (value != null) {
            return value;
        } else {
            HashMap<String, Condition> scanFilter = new HashMap<>();
            Condition condition = new Condition()
                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                    .withAttributeValueList(new AttributeValue(key));
            scanFilter.put(KEY_REQUEST_PRIMARY_KEY, condition);
            ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
            ScanResult scanResult = dynamoDB.scan(scanRequest);
            List<Map<String, AttributeValue>> items = scanResult.getItems();
            if (items.size() > 0) {
                Map<String, AttributeValue> item = items.get(0);
                value = Long.parseLong(item.get(KEY_REQUEST_COST).getN());
                requestCostCache.put(cacheKey, value);
                return value;
            } else {
                return null;
            }
        }
    }


    private static void createStatsTable(String name) {
        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(name)
                .withKeySchema(new KeySchemaElement().withAttributeName(KEY_REQUEST_PRIMARY_KEY).withKeyType(KeyType.HASH))
                .withAttributeDefinitions(new AttributeDefinition().withAttributeName(KEY_REQUEST_PRIMARY_KEY).withAttributeType(ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

        TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
    }

    private static String getKey(SolverArgumentParser parser) {
        return String.format("N1:%d&N2:%d&UN:%d&BOARD:%s", parser.getN1(), parser.getN2(), parser.getUn(), parser.getInputBoard());
    }


    private static String getKey(SudokuParameters parameters) {
        return String.format("N1:%d&N2:%d&UN:%d&BOARD:%s", parameters.getN1(), parameters.getN2(), parameters.getUn(), parameters.getInputBoard());
    }

    private static String getCacheKey(String key, String tableName) {
        return key + tableName;
    }

    //Prevent utility class initialization
    private DynamoFrontEnd() {
    }
}
