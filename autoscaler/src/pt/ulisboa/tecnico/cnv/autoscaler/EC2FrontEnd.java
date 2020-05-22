package pt.ulisboa.tecnico.cnv.autoscaler;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import pt.ulisboa.tecnico.cnv.autoscaler.task.CreateInstanceTask;
import pt.ulisboa.tecnico.cnv.autoscaler.task.RemoveInstanceTask;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.InstanceManager;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.ThreadManager;

import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class EC2FrontEnd {
    private static final String REGION = "us-east-1";
    private static final int INSTANCE_RUNNING_CODE = 16;
    private static final int MIN_COUNT = 1;
    private static final int MAX_COUNT = 1;
    private static final int WAIT_TIME = 5000;
    private static final String AMI_ID = "ami-00f23826215163ef7";
    private static final String KEY_PAIR_NAME = "CNV-Project-Pair";
    private static final String SECURITY_GROUP_NAME = "CNV-Project";
    private static final String INSTANCE_TYPE = "t2.micro";

    private static AmazonEC2 ec2;
    private static AtomicInteger numInstances = new AtomicInteger(0);

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

    private EC2FrontEnd() {
    }

    public static CreateInstanceTask createInstance() {
        EC2FrontEnd.numInstances.incrementAndGet();
        CreateInstanceTask task = new CreateInstanceTask();
        ThreadManager.execute(task);
        return task;
    }

    public static void newInstance() {
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
                .withImageId(AMI_ID)
                .withInstanceType(INSTANCE_TYPE)
                .withMinCount(MIN_COUNT)
                .withMaxCount(MAX_COUNT)
                .withKeyName(KEY_PAIR_NAME)
                .withSecurityGroups(SECURITY_GROUP_NAME);

        RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
        String instanceId = runInstancesResult.getReservation().getInstances().get(0).getInstanceId();
        System.out.println("Created instance " + instanceId + " waiting for it to start...");

        String instanceDns = waitInstanceRunning(instanceId);
        String url = String.format("http://%s:8000", instanceDns);
        try {
            System.out.println("Adding instance " + instanceId + " to load balancer");
            InstanceManager.addInstance(url, instanceId);
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL " + url + " unable to add instance");
        }

    }

    private static String waitInstanceRunning(String instanceId) {
        while (true) {
            //Checks if instance is already running
            try {
                Thread.sleep(WAIT_TIME);
                Instance instance = getInstance(instanceId);
                if (instance.getState().getCode() == INSTANCE_RUNNING_CODE) {
                    System.out.println("Instance " + instanceId + " is running!");
                    return instance.getPublicDnsName();
                }
            } catch (InterruptedException e) {
                System.out.println("Error: Auto Scaling thread interrupted while creating instance");
            } catch (com.amazonaws.services.ec2.model.AmazonEC2Exception e) {
                System.out.println("Instance not yet available, trying again");
            }
        }
    }


    private static Instance getInstance(String instanceId) {
        DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(instanceId);
        DescribeInstancesResult describeInstancesResult = ec2.describeInstances(request);
        List<Reservation> reservations = describeInstancesResult.getReservations();
        List<com.amazonaws.services.ec2.model.Instance> instances = reservations.get(0).getInstances();
        return instances.get(0);
    }

    public static void terminateInstance(pt.ulisboa.tecnico.cnv.loadbalancer.instance.Instance instance) {
        numInstances.decrementAndGet();
        ThreadManager.execute(new RemoveInstanceTask(instance));
    }

    public static void terminateInstanceRequest(pt.ulisboa.tecnico.cnv.loadbalancer.instance.Instance instance) {
        TerminateInstancesRequest request = new TerminateInstancesRequest().withInstanceIds(instance.getId());
        ec2.terminateInstances(request);
    }

    public static int getNumInstances() {
        return numInstances.get();
    }

}
