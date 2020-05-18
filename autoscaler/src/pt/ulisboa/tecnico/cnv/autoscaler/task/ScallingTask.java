package pt.ulisboa.tecnico.cnv.autoscaler.task;

import pt.ulisboa.tecnico.cnv.autoscaler.AutoScaler;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.InstanceManager;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.ThreadManager;

import java.util.Date;

public class ScallingTask implements Runnable {
    //TODO - Implement default cooldown
    //TODO - MIN AND MAX INSTANCES
    private static final long DEFAULT_COOLDOWN = 3 * 60 * 1000; //3 minutes
    private static final int MAX_LOAD_TIME = 120000; //2 minutes
    private static final int NUMBER_MEASURES = 10; //Number of measures per max load time
    private static final int TIME = MAX_LOAD_TIME / NUMBER_MEASURES;
    //TODO - Calculate scale up and down values
    private static final long SCALE_UP_VALUE_THRESHOLD = 10000;
    private static final long SCALE_DOWN_VALUE_THRESHOLD = 1000;

    private long[] loadValues = new long[NUMBER_MEASURES];
    private long lastScaleTime = 0;

    @Override
    public void run() {
        int round = 0;
        while(true){
            try {
                Thread.sleep(TIME);
                loadValues[round] = InstanceManager.getTotalLoad();
                scallingPolicy();
                round = ++round % NUMBER_MEASURES;
            } catch (InterruptedException e) {
                System.out.println("Scalling Task failed. Aborting");
                return;
            }
        }
    }

    private void scallingPolicy(){
        long averageLoad = calculateLoadAverage();

        if(averageLoad >= SCALE_UP_VALUE_THRESHOLD){
            System.out.println("Scale Up Threshold achieved");

            Date date = new Date();
            if(date.getTime() - lastScaleTime >= DEFAULT_COOLDOWN){
                System.out.println("Adding new instance");
                ThreadManager.execute(new CreateInstanceTask());
                lastScaleTime = date.getTime();
            }
        }

        else if(averageLoad <= SCALE_DOWN_VALUE_THRESHOLD){
            System.out.println("Scale Down Threshold achieved");
            String instanceId = InstanceManager.getInstanceToRemove();

            Date date = new Date();
            if(date.getTime() - lastScaleTime >= DEFAULT_COOLDOWN){
                System.out.println("Removing instance " + instanceId);
                ThreadManager.execute(new RemoveInstanceTask(instanceId));
                lastScaleTime = date.getTime();
            }
        }
    }

    /*******************/
    /** Aux functions **/
    /*******************/


    private long calculateLoadAverage(){
        long totalLoad = 0;
        for(long load : loadValues){
            totalLoad += load;
        }
        return totalLoad / NUMBER_MEASURES;
    }


}
