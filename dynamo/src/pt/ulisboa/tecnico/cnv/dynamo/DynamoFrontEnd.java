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
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;

import java.util.HashMap;
import java.util.Map;

public class DynamoFrontEnd {
    private static final String REGION = "us-east-1";
    private static final String KEY_REQUEST_PRIMARY_KEY = "RequestQuery";
    private static final String KEY_BOARD_SIZE_N1 = "BoardSizeN1";
    private static final String KEY_BOARD_SIZE_N2 = "BoardSizeN2";
    private static final String KEY_UNASSIGNED_ENTRIES = "UnassignedEntries";
    private static final String KEY_BOARD_IDENTIFIER = "BoardIdentifier";
    private static final String KEY_REQUEST_COST = "RequestCost";

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

    //Prevent utility class initialization
    private DynamoFrontEnd() {
    }
}
