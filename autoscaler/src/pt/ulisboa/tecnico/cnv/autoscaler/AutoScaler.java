package pt.ulisboa.tecnico.cnv.autoscaler;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.InstanceManager;

public class AutoScaler {

    //Code for instance Running
    private int RUNNING = 16;

    private static AutoScaler autoScaler;

    private AmazonEC2 ec2;

    private String region = "us-east-1";

    //Instances information
    private String AMIid = "ami-0c56c418f2f4f7df4";
    private String keyPairName = "CNV-Project-Pair";
    private String securityGroupName = "CNV-ssh+http";

    private AutoScaler(){
        AWSCredentials credentials = null;
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
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }

    public static AutoScaler getInstance(){
        if(autoScaler == null){
            autoScaler = new AutoScaler();
        }
        return autoScaler;
    }

    public void createInstance(){
        RunInstancesRequest runInstancesRequest =
                new RunInstancesRequest();

        runInstancesRequest.withImageId(AMIid)
                .withInstanceType("t2.micro")
                .withMinCount(1)
                .withMaxCount(1)
                .withKeyName(keyPairName)
                .withSecurityGroups(securityGroupName);

        RunInstancesResult runInstancesResult =
                ec2.runInstances(runInstancesRequest);

        //Get instance IP
        String instanceId = runInstancesResult.getReservation().getInstances().get(0).getInstanceId();
        System.out.println("Created instance " + instanceId);

        while(true){
            //Checks if instance is already running
            Instance instance = getCreatedInstance(instanceId);
            if(instance.getState().getCode() == RUNNING){
                System.out.println("Instance is running! Adding to LoadBalancer");
                try {
                    InstanceManager.getInstance().addInstance(instance.getPublicIpAddress(), instance.getInstanceId());
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

    public void terminateInstance(String instanceId){
        InstanceManager.getInstance().removeInstance(instanceId);
        System.out.println("Removed instance " + instanceId);

        TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
        termInstanceReq.withInstanceIds(instanceId);
        ec2.terminateInstances(termInstanceReq);
    }

    private Instance getCreatedInstance(String instanceId){
        DescribeInstancesRequest request = new DescribeInstancesRequest();
        ArrayList<String> instanceCreated = new ArrayList<>();
        instanceCreated.add(instanceId);

        request.setInstanceIds(instanceCreated);
        DescribeInstancesResult describeInstancesResult = ec2.describeInstances(request);

        //TODO - Assuming only 1 reservation with 1 instance returns. Check if it is correct

        List<Reservation> reservations = describeInstancesResult.getReservations();
        List<Instance> instances = reservations.get(0).getInstances();
        return  instances.get(0);
    }

}
