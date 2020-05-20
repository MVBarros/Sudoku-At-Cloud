package pt.ulisboa.tecnico.cnv.autoscaler.task;

import pt.ulisboa.tecnico.cnv.autoscaler.EC2FrontEnd;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.Instance;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.InstanceManager;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.SystemLoad;

import java.lang.Math;

public class ScalingTask implements Runnable {
    public static final int MIN_NUMBER_INSTANCES = 2;

    private static final long SCALE_COOLDOWN = 3 * 60 * 1000;
    private static final int NUMBER_MEASURES = 10;
    private static final int TIME_BETWEEN_MEASUREMENTS = 5000;
    private static final long SCALE_UP_LOAD_THRESHOLD = (long) (2.5 * Math.pow(10, 9));
    private static final long SCALE_DOWN_LOAD_THRESHOLD = (long) (8 * Math.pow(10, 8));
    private static final long SCALE_UP_AVG_REQUEST_THRESHOLD = 3;
    private static final long SCALE_DOWN_AVG_REQUEST_THRESHOLD = 1;


    private SystemLoad[] measurements = new SystemLoad[NUMBER_MEASURES];
    private long lastScaleTimestamp = System.currentTimeMillis();

    @Override
    public void run() {
        int index = 0;
        while (true) {
            try {
                Thread.sleep(TIME_BETWEEN_MEASUREMENTS);
                SystemLoad currentLoad = InstanceManager.getSystemInfo();
                System.out.println("Current system load: " + currentLoad.getSystemLoad());
                System.out.println("Current number of requests: " + currentLoad.getAvgNumberRequests());

                measurements[index] = currentLoad;
                scalingPolicy();
                index = (index + 1) % NUMBER_MEASURES;
            } catch (InterruptedException e) {
                System.out.println("Error: Scaling Task was interrupted");
            }
        }
    }

    private void scalingPolicy() {
        long currentTime = System.currentTimeMillis();
        int numInstances = EC2FrontEnd.getNumInstances();
        System.out.println("Current number of instances: " + numInstances);
        if (numInstances < MIN_NUMBER_INSTANCES) {
            System.out.println("Instances dropped below minimum, adding new instance");
            addInstance(currentTime);
            return;
        }
        if (currentTime - lastScaleTimestamp < SCALE_COOLDOWN) {
            return;
        }
        SystemLoad averageLoad = calculateLoadAverage();

        if (averageLoad.getSystemLoad() >= SCALE_UP_LOAD_THRESHOLD &&
                averageLoad.getAvgNumberRequests() >= SCALE_UP_AVG_REQUEST_THRESHOLD) {
            System.out.println("Scale Up Threshold achieved, adding new instance");
            addInstance(currentTime);
        } else if (averageLoad.getSystemLoad() <= SCALE_DOWN_LOAD_THRESHOLD &&
                averageLoad.getAvgNumberRequests() <= SCALE_DOWN_AVG_REQUEST_THRESHOLD &&
                numInstances > MIN_NUMBER_INSTANCES) {
            removeInstance(currentTime);
        }
    }

    private SystemLoad calculateLoadAverage() {
        long totalLoad = 0;
        long totalRequests = 0;
        for (SystemLoad load : measurements) {
            totalLoad += load.getSystemLoad();
            totalRequests += load.getAvgNumberRequests();
        }
        long averageLoad = totalLoad / NUMBER_MEASURES;
        long averageRequests = totalRequests / NUMBER_MEASURES;
        System.out.println("Current average load: " + averageLoad);
        System.out.println("Current average number requests: " + averageRequests);
        return new SystemLoad(averageLoad, averageRequests);
    }

    private void addInstance(long currentTime) {
        EC2FrontEnd.createInstance();
        lastScaleTimestamp = currentTime;
    }

    private void removeInstance(long currentTime) {
        Instance instance = InstanceManager.removeInstance();
        if (instance == null) {
            //Return
            System.out.println("Instance to remove was null...");
            return;
        }
        System.out.println("Scale Down Threshold achieved, removing instance " + instance.getId());
        EC2FrontEnd.terminateInstance(instance);
        lastScaleTimestamp = currentTime;
    }


}
