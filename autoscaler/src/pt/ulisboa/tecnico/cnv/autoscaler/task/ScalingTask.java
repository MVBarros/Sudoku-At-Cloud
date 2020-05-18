package pt.ulisboa.tecnico.cnv.autoscaler.task;

import pt.ulisboa.tecnico.cnv.loadbalancer.instance.InstanceManager;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.ThreadManager;

public class ScalingTask implements Runnable {
    //FIXME AUTOSCALER SHOULD KEEP NUMBER OF INSTANCES THAT EXIST (INDEPENDENT OF TIMINGS), WHICH IS THE ONE THAT IS USED BY THIS THREAD
    public static final int MIN_NUMBER_INSTANCES = 1;

    private static final long SCALE_COOLDOWN = 3 * 60 * 1000;
    private static final int NUMBER_MEASURES = 10;
    private static final int TIME_BETWEEN_MEASUREMENTS = 5000;
    private static final long SCALE_UP_VALUE_THRESHOLD = 10000; //TODO
    private static final long SCALE_DOWN_VALUE_THRESHOLD = 1000; //TODO

    private long[] measurements = new long[NUMBER_MEASURES];
    private long lastScaleTimestamp = System.currentTimeMillis();

    @Override
    public void run() {
        int index = 0;
        while (true) {
            try {
                Thread.sleep(TIME_BETWEEN_MEASUREMENTS);
                measurements[index] = InstanceManager.getTotalLoad();
                scalingPolicy();
                index = ++index % NUMBER_MEASURES;
            } catch (InterruptedException e) {
                System.out.println("Error: Scaling Task was interrupted");
            }
        }
    }

    private void scalingPolicy() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastScaleTimestamp < SCALE_COOLDOWN) {
            return;
        }
        int numInstances = InstanceManager.getNumInstances();
        long averageLoad = calculateLoadAverage();

        if (numInstances < MIN_NUMBER_INSTANCES) {
            ThreadManager.execute(new CreateInstanceTask());
            lastScaleTimestamp = currentTime;
        } else if (averageLoad >= SCALE_UP_VALUE_THRESHOLD) {
            System.out.println("Scale Up Threshold achieved, adding new instance");
            ThreadManager.execute(new CreateInstanceTask());
            lastScaleTimestamp = currentTime;
        } else if (averageLoad <= SCALE_DOWN_VALUE_THRESHOLD && numInstances > MIN_NUMBER_INSTANCES) {
            String instanceId = InstanceManager.getInstanceToRemove();
            System.out.println("Scale Down Threshold achieved, removing instance " + instanceId);
            ThreadManager.execute(new RemoveInstanceTask(instanceId));
            lastScaleTimestamp = currentTime;
        }
    }

    private long calculateLoadAverage() {
        long totalLoad = 0;
        for (long load : measurements) {
            totalLoad += load;
        }
        return totalLoad / NUMBER_MEASURES;
    }


}
