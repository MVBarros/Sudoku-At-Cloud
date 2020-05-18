package pt.ulisboa.tecnico.cnv.autoscaler.task;

import com.amazonaws.services.ec2.model.*;
import pt.ulisboa.tecnico.cnv.autoscaler.AutoScaler;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.InstanceManager;

import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class CreateInstanceTask implements Runnable {

    //Instances information
    private static final int INSTANCE_RUNNING_CODE = 16;
    private static final int MIN_COUNT = 1;
    private static final int MAX_COUNT = 1;
    private static final String AMI_ID = "ami-0c56c418f2f4f7df4";
    private static final String KEY_PAIR_NAME = "CNV-Project-Pair";
    private static final String SECURITY_GROUP_NAME = "CNV-Project";
    private static final String INSTANCE_TYPE = "t2.micro";

    private CountDownLatch latch = new CountDownLatch(1);
    @Override
    public void run() {

        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

        runInstancesRequest.withImageId(AMI_ID)
                .withInstanceType(INSTANCE_TYPE)
                .withMinCount(MIN_COUNT)
                .withMaxCount(MAX_COUNT)
                .withKeyName(KEY_PAIR_NAME)
                .withSecurityGroups(SECURITY_GROUP_NAME);

        RunInstancesResult runInstancesResult =
                AutoScaler.getEc2().runInstances(runInstancesRequest);

        //Get instance IP
        String instanceId = runInstancesResult.getReservation().getInstances().get(0).getInstanceId();
        System.out.println("Created instance " + instanceId);

        while (true) {
            //Checks if instance is already running
            try {
                Thread.sleep(5000);
                com.amazonaws.services.ec2.model.Instance instance = getCreatedInstance(instanceId);
                if (instance.getState().getCode() == INSTANCE_RUNNING_CODE) {
                    System.out.println("Instance is running! Adding to LoadBalancer");
                    InstanceManager.addInstance("http://" + instance.getPublicDnsName() + ":8000", instance.getInstanceId());
                    latch.countDown();
                    return;
                }
            } catch (MalformedURLException e) {
                System.out.println("Malformed URL, unable to add instance");
            } catch (InterruptedException e) {
                System.out.println("Error: Auto Scaling thread interrupted while creating instance");
            } catch (com.amazonaws.services.ec2.model.AmazonEC2Exception e){
                System.out.println("Instance not yet available, trying again");
            }
        }


    }

    private static com.amazonaws.services.ec2.model.Instance getCreatedInstance(String instanceId) {
        DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(instanceId);
        DescribeInstancesResult describeInstancesResult = AutoScaler.getEc2().describeInstances(request);
        //TODO - Assuming only 1 reservation with 1 instance returns. Check if it is correct

        List<Reservation> reservations = describeInstancesResult.getReservations();
        List<com.amazonaws.services.ec2.model.Instance> instances = reservations.get(0).getInstances();
        return instances.get(0);
    }

    public void waitFinish() throws InterruptedException {
        latch.await();
    }

}
