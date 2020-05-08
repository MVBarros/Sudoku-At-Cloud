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
import pt.ulisboa.tecnico.cnv.server.MetricUtils;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DynamoFrontEnd {
    private static final String REGION = "us-east-1";

    private static final String KEY_ATTRIBUTE_NAME = "key";

    private static final String KEY_BOARD_SIZE = "Board-Size";
    private static final String KEY_UNASSIGNED_ENTRIES = "Unassigned-Entries";
    private static final String KEY_BOARD_IDENTIFIER = "Board-Identifier";
    private static final String KEY_UN_PARAMETER = "UN-Parameter";
    private static final String KEY_REQUEST_COST = "Request-Cost";

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
        } catch (InterruptedException e) {
            System.out.println("ERROR: was interrupted while creating tables");
            System.exit(1);
        }
    }

    public static void uploadStats(SolverArgumentParser parser, Stats stats) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(KEY_BOARD_SIZE, new AttributeValue().withN(Integer.toString(parser.getN1())));
        item.put(KEY_BOARD_IDENTIFIER, new AttributeValue(parser.getInputBoard()));
        item.put(KEY_UN_PARAMETER, new AttributeValue().withN(Integer.toString(parser.getUn())));
        item.put(KEY_UNASSIGNED_ENTRIES, new AttributeValue().withN(Integer.toString(MetricUtils.getBoardZeros(parser))));
        item.put(KEY_REQUEST_COST, new AttributeValue().withN(Long.toString(stats.getCost())));
        item.put(KEY_ATTRIBUTE_NAME, new AttributeValue(randomKey()));
        PutItemRequest putItemRequest = new PutItemRequest(stats.getTableName(), item);
        dynamoDB.putItem(putItemRequest);
    }

    private static void createStatsTable(String name) throws InterruptedException {
        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(name)
                .withKeySchema(new KeySchemaElement().withAttributeName(KEY_ATTRIBUTE_NAME).withKeyType(KeyType.HASH))
                .withAttributeDefinitions(new AttributeDefinition().withAttributeName(KEY_ATTRIBUTE_NAME).withAttributeType(ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

        TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
        TableUtils.waitUntilActive(dynamoDB, name);
    }

    private static String randomKey() {
        return UUID.randomUUID().toString();
    }


    //Prevent utility class initialization
    private DynamoFrontEnd() {
    }
}
