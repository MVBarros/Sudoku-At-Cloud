package pt.ulisboa.tecnico.cnv.dynamo;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import metrics.tools.StatsBFS;
import metrics.tools.StatsCP;
import metrics.tools.StatsDLX;
import pt.ulisboa.tecnico.cnv.server.MetricUtils;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;

import java.util.HashMap;
import java.util.Map;

public class DynamoFrontEnd {
    private static final String REGION = "us-east-1";
    private static final String BFS_TABLE_NAME = "BFS-Stats";
    private static final String CP_TABLE_NAME = "CP-Stats";
    private static final String DLX_TABLE_NAME = "DLX-Stats";
    private static final String KEY_ATTRIBUTE_NAME = "key";

    private static final String KEY_BOARD_SIZE = "Board-Size";
    private static final String KEY_UNASSIGNED_ENTRIES = "Unassigned-Entries";
    private static final String KEY_BOARD_IDENTIFIER = "Board-Identifier";
    private static final String KEY_UN_PARAMETER = "UN-Parameter";
    private static final String KEY_REQUEST_COST = "Request-Cost";

    private static final long BFS_COST_SCALE = 2500;
    private static final long CP_COST_SCALE = 2500;
    private static final long DLX_COST_SCALE = 2500;

    private static AmazonDynamoDB dynamoDB;

    private static void init() {
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
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

    private static AmazonDynamoDB getDB() {
        if (dynamoDB == null) {
            init();
        }
        return dynamoDB;
    }

    public static void createTables() {
        try {
            createBFSTable();
            createCPTable();
            createDLXTable();
        } catch(InterruptedException e) {
            System.out.println("Error occurred: was interrupted while creating tables");
            System.exit(1);
        }

    }

    private static void createBFSTable() throws InterruptedException {
        createStatsTable(BFS_TABLE_NAME);

    }

    private static void createCPTable() throws InterruptedException {
        createStatsTable(CP_TABLE_NAME);
    }

    private static void createDLXTable() throws InterruptedException {
        createStatsTable(DLX_TABLE_NAME);
    }

    //To Create a Table we just need to give it a primary key since the table itself is schema less.
    private static void createStatsTable(String name) throws InterruptedException {
        // Create a table with a primary hash string key
        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(name)
                .withKeySchema(new KeySchemaElement().withAttributeName(KEY_ATTRIBUTE_NAME).withKeyType(KeyType.HASH))
                .withAttributeDefinitions(new AttributeDefinition().withAttributeName(KEY_ATTRIBUTE_NAME).withAttributeType(ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

        // Create table if it does not exist yet
        TableUtils.createTableIfNotExists(getDB(), createTableRequest);
        // wait for the table to move into ACTIVE state
        TableUtils.waitUntilActive(getDB(), BFS_TABLE_NAME);
    }

    public static void uploadStatsBFS(SolverArgumentParser parser, StatsBFS stats) {
        Map<String, AttributeValue> item = new HashMap<>();
        setParserAttributes(item, parser);
        long requestCost = stats.getMethodCount() * BFS_COST_SCALE;
        item.put(KEY_REQUEST_COST, new AttributeValue().withN(Long.toString(requestCost)));
        uploadStatsTable(BFS_TABLE_NAME, item);
    }

    public static void uploadStatsCP(SolverArgumentParser parser, StatsCP stats) {
        Map<String, AttributeValue> item = new HashMap<>();
        setParserAttributes(item, parser);
        long requestCost = stats.getNewCount() * CP_COST_SCALE;
        item.put(KEY_REQUEST_COST, new AttributeValue().withN(Long.toString(requestCost)));
        uploadStatsTable(CP_TABLE_NAME, item);
    }

    public static void uploadStatsDLX(SolverArgumentParser parser, StatsDLX stats) {
        Map<String, AttributeValue> item = new HashMap<>();
        setParserAttributes(item, parser);
        long requestCost = stats.getNewArrayCount() * DLX_COST_SCALE;
        item.put(KEY_REQUEST_COST, new AttributeValue().withN(Long.toString(requestCost)));
        uploadStatsTable(DLX_TABLE_NAME, item);
    }

    private static void setParserAttributes(Map<String, AttributeValue> item, SolverArgumentParser parser) {
        item.put(KEY_BOARD_SIZE, new AttributeValue().withN(Integer.toString(parser.getN1())));
        item.put(KEY_BOARD_IDENTIFIER, new AttributeValue(parser.getInputBoard()));
        item.put(KEY_UN_PARAMETER, new AttributeValue().withN(Integer.toString(parser.getUn())));
        item.put(KEY_UNASSIGNED_ENTRIES, new AttributeValue().withN(Integer.toString(MetricUtils.getBoardZeros(parser))));
    }


    //To Create a Table we just need to give it a primary key since the table itself is schema less.
    private static void uploadStatsTable(String tableName, Map<String, AttributeValue> item) {
        PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
        PutItemResult putItemResult = getDB().putItem(putItemRequest);
        System.out.println("Result of putting item: " + putItemResult);
    }

    //Prevent utility class initialization
    private DynamoFrontEnd() {}
}
