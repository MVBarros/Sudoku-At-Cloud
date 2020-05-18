package pt.ulisboa.tecnico.cnv.autoscaler;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.InstanceManager;

import java.net.MalformedURLException;
import java.util.List;

public class AutoScaler {

    //Instances information
    private static final int INSTANCE_RUNNING_CODE = 16;
    private static final int MIN_COUNT = 1;
    private static final int MAX_COUNT = 1;
    private static final String REGION = "us-east-1";
    private static final String AMI_ID = "ami-0c56c418f2f4f7df4";
    private static final String KEY_PAIR_NAME = "CNV-Project-Pair";
    private static final String SECURITY_GROUP_NAME = "CNV-Project";
    private static final String INSTANCE_TYPE = "t2.micro";

    private static AmazonEC2 ec2;

    static {
        AWSCredentials credentials;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        ec2 = AmazonEC2ClientBuilder.standard()
                .withRegion(REGION)
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }

    public AutoScaler() {
    }

    public static void createInstance() {
        RunInstancesRequest runInstancesRequest =
                new RunInstancesRequest();

        runInstancesRequest.withImageId(AMI_ID)
                .withInstanceType(INSTANCE_TYPE)
                .withMinCount(MIN_COUNT)
                .withMaxCount(MAX_COUNT)
                .withKeyName(KEY_PAIR_NAME)
                .withSecurityGroups(SECURITY_GROUP_NAME);

        RunInstancesResult runInstancesResult =
                ec2.runInstances(runInstancesRequest);

        //Get instance IP
        String instanceId = runInstancesResult.getReservation().getInstances().get(0).getInstanceId();
        System.out.println("Created instance " + instanceId);

        while (true) {
            //Checks if instance is already running
            com.amazonaws.services.ec2.model.Instance instance = getCreatedInstance(instanceId);
            if (instance.getState().getCode() == INSTANCE_RUNNING_CODE) {
                System.out.println("Instance is running! Adding to LoadBalancer");
                try {
                    InstanceManager.addInstance(instance.getPublicDnsName(), instance.getInstanceId());
                } catch (MalformedURLException e) {
                    System.out.println("Malformed URL, unable to add instance");
                }
                return;
            }
            //Else, check wait a bit to try again
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static void terminateInstance(String instanceId) {
        InstanceManager.removeInstance(instanceId);
        System.out.println("Removed instance " + instanceId);

        //TODO Wait for instance to finish it's requests
        TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
        termInstanceReq.withInstanceIds(instanceId);
        ec2.terminateInstances(termInstanceReq);
    }

    private static com.amazonaws.services.ec2.model.Instance getCreatedInstance(String instanceId) {
        DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(instanceId);
        DescribeInstancesResult describeInstancesResult = ec2.describeInstances(request);
        //TODO - Assuming only 1 reservation with 1 instance returns. Check if it is correct

        List<Reservation> reservations = describeInstancesResult.getReservations();
        List<com.amazonaws.services.ec2.model.Instance> instances = reservations.get(0).getInstances();
        return instances.get(0);
    }

}
