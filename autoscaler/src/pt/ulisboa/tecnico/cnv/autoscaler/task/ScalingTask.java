package pt.ulisboa.tecnico.cnv.autoscaler.task;

import pt.ulisboa.tecnico.cnv.autoscaler.AutoScaler;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.InstanceManager;

public class ScalingTask implements Runnable {
    public static final int MIN_NUMBER_INSTANCES = 2;

    private static final long SCALE_COOLDOWN = 3 * 60 * 1000;
    private static final int NUMBER_MEASURES = 10;
    private static final int TIME_BETWEEN_MEASUREMENTS = 5000;
    private static final long SCALE_UP_VALUE_THRESHOLD = 100000; //TODO
    private static final long SCALE_DOWN_VALUE_THRESHOLD = 1000; //TODO

    private long[] measurements = new long[NUMBER_MEASURES];
    private long lastScaleTimestamp = System.currentTimeMillis();

    @Override
    public void run() {
        int index = 0;
        while (true) {
            try {
                Thread.sleep(TIME_BETWEEN_MEASUREMENTS);
                long currentLoad = InstanceManager.getTotalLoad();
                System.out.println("Current system load: " + currentLoad);
                measurements[index] = currentLoad;
                scalingPolicy();
                index = ++index % NUMBER_MEASURES;
            } catch (InterruptedException e) {
                System.out.println("Error: Scaling Task was interrupted");
            }
        }
    }

    private void scalingPolicy() {
        long currentTime = System.currentTimeMillis();
        int numInstances = AutoScaler.getNumInstances();
        if (numInstances < MIN_NUMBER_INSTANCES) {
            System.out.println("Instances dropped below minimum, adding new instance");
            addInstance(currentTime);
            return;
        }
        if (currentTime - lastScaleTimestamp < SCALE_COOLDOWN) {
            return;
        }
        long averageLoad = calculateLoadAverage();

        if (averageLoad >= SCALE_UP_VALUE_THRESHOLD) {
            System.out.println("Scale Up Threshold achieved, adding new instance");
            addInstance(currentTime);
        } else if (averageLoad <= SCALE_DOWN_VALUE_THRESHOLD && numInstances > MIN_NUMBER_INSTANCES) {
            removeInstance(currentTime);
        }
    }

    private long calculateLoadAverage() {
        long totalLoad = 0;
        for (long load : measurements) {
            totalLoad += load;
        }
        long average = totalLoad / NUMBER_MEASURES;
        System.out.println("Current average cost: " + average);
        return average;
    }

    private void addInstance(long currentTime) {
        AutoScaler.createInstance();
        lastScaleTimestamp = currentTime;
    }

    private void removeInstance(long currentTime) {
        String instanceId = InstanceManager.getInstanceToRemove();
        System.out.println("Scale Down Threshold achieved, removing instance " + instanceId);
        AutoScaler.terminateInstance(instanceId);
        lastScaleTimestamp = currentTime;
    }


}
