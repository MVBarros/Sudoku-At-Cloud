package pt.ulisboa.tecnico.cnv.autoscaler.task;

import pt.ulisboa.tecnico.cnv.autoscaler.AutoScaler;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.InstanceManager;

public class ScallingTask implements Runnable {
    //TODO - Define Max time and interval
    private static final int MAX_LOAD_TIME = 120000; //2 minutes
    private static final int TIME = MAX_LOAD_TIME / 10; // 12 seconds
    //TODO - Calculate scale up and down values
    private static final long SCALE_UP_VALUE_THRESHOLD = 10000;
    private static final long SCALE_DOWN_VALUE_THRESHOLD = 1000;
    private static final int INTERVAL = MAX_LOAD_TIME / TIME;

    private long[] loadValues = new long[INTERVAL];

    private AutoScaler scaler = AutoScaler.getInstance();
    private InstanceManager loadBalancer = InstanceManager.getInstance();

    @Override
    public void run() {
        setupVector();
        int round = 0;
        while(true){
            try {
                Thread.sleep(TIME);
                loadValues[round] = loadBalancer.getTotalLoad();
                scallingPolicy();
                round = ++round % INTERVAL;
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
            scaler.createInstance();
        }

        else if(averageLoad <= SCALE_DOWN_VALUE_THRESHOLD){
            System.out.println("Scale Down Threshold achieved");
            String instanceId = loadBalancer.getInstanceToRemove();

            System.out.println("Removing instance " + instanceId);
            scaler.terminateInstance(instanceId);
        }
    }

    /*******************/
    /** Aux functions **/
    /*******************/

    private void setupVector(){
        for(int i = 0; i < INTERVAL; i ++){
            loadValues[i] = 0;
        }
    }

    private long calculateLoadAverage(){
        long totalLoad = 0;
        for(long load : loadValues){
            totalLoad += load;
        }
        return totalLoad / INTERVAL;
    }


}
