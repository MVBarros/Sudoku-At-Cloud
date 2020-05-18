package pt.ulisboa.tecnico.cnv.autoscaler;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import pt.ulisboa.tecnico.cnv.autoscaler.task.CreateInstanceTask;
import pt.ulisboa.tecnico.cnv.autoscaler.task.RemoveInstanceTask;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.InstanceManager;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.ThreadManager;

import java.util.concurrent.atomic.AtomicInteger;

public class AutoScaler {
    private static final String REGION = "us-east-1";


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

    public AutoScaler() {
    }

    public static void createInstance() {
        AutoScaler.numInstances.incrementAndGet();
        ThreadManager.execute(new CreateInstanceTask());
    }

    public static void terminateInstance(String instanceId) {
        pt.ulisboa.tecnico.cnv.loadbalancer.instance.Instance removedInstance = InstanceManager.removeInstance(instanceId);
        if (removedInstance != null) {
            AutoScaler.numInstances.decrementAndGet();
            ThreadManager.execute(new RemoveInstanceTask(removedInstance));
        } else {
            System.out.println("Error: Trying to remove instance with id " + instanceId + " but it does not exist");
        }
    }

    public static int getNumInstances() {
        return numInstances.get();
    }

    public static AmazonEC2 getEc2() {
        return ec2;
    }
}
