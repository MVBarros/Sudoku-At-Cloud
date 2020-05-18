package pt.ulisboa.tecnico.cnv.autoscaler.task;

import pt.ulisboa.tecnico.cnv.loadbalancer.instance.InstanceManager;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.ThreadManager;

public class ScalingTask implements Runnable {
    private static final int MIN_COUNT = 1;

    private static final long DEFAULT_COOLDOWN = 3 * 60 * 1000; //3 minutes
    private static final int MAX_LOAD_TIME = 120000; //2 minutes
    private static final int NUMBER_MEASURES = 10; //Number of measures per max load time
    private static final int TIME = MAX_LOAD_TIME / NUMBER_MEASURES;
    //TODO - Calculate scale up and down values
    private static final long SCALE_UP_VALUE_THRESHOLD = 10000;
    private static final long SCALE_DOWN_VALUE_THRESHOLD = 1000;

    private long[] loadValues = new long[NUMBER_MEASURES];
    private long lastScaleTime = System.currentTimeMillis();

    @Override
    public void run() {
        int round = 0;
        while (true) {
            try {
                Thread.sleep(TIME);
                loadValues[round] = InstanceManager.getTotalLoad();
                scalingPolicy();
                round = ++round % NUMBER_MEASURES;
            } catch (InterruptedException e) {
                System.out.println("Scaling Task failed. Aborting");
                return;
            }
        }
    }

    private void scalingPolicy() {
        long averageLoad = calculateLoadAverage();

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastScaleTime < DEFAULT_COOLDOWN) {
            return;
        }
        int numInstances = InstanceManager.getNumInstances();
        if (numInstances < MIN_COUNT) {
            ThreadManager.execute(new CreateInstanceTask());
            lastScaleTime = currentTime;
        } else if (averageLoad >= SCALE_UP_VALUE_THRESHOLD) {
            System.out.println("Scale Up Threshold achieved");
            System.out.println("Adding new instance");
            ThreadManager.execute(new CreateInstanceTask());
            lastScaleTime = currentTime;

        } else if (averageLoad <= SCALE_DOWN_VALUE_THRESHOLD && numInstances > MIN_COUNT) {
            System.out.println("Scale Down Threshold achieved");
            String instanceId = InstanceManager.getInstanceToRemove();
            System.out.println("Removing instance " + instanceId);
            ThreadManager.execute(new RemoveInstanceTask(instanceId));
            lastScaleTime = currentTime;
        }
    }

    private long calculateLoadAverage() {
        long totalLoad = 0;
        for (long load : loadValues) {
            totalLoad += load;
        }
        return totalLoad / NUMBER_MEASURES;
    }


}
