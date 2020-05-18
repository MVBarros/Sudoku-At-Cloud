package pt.ulisboa.tecnico.cnv.loadbalancer.task;

import pt.ulisboa.tecnico.cnv.loadbalancer.instance.Instance;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.state.InstanceStateDead;


public class HealthCheckTask implements Runnable {
    private static final int INTERVAL = 10000; //10 seconds
    private static final int GRACE_PERIOD = 30000; //30 seconds


    private final Instance instance;

    public HealthCheckTask(Instance instance) {
        this.instance = instance;
    }

    @Override
    public void run() {

        try {
            Thread.sleep(GRACE_PERIOD);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                if (instance.healthCheck()) {
                    instance.resetFailureCounter();
                } else {
                    instance.incrFailureCounter();
                }
                if (instance.getState() == InstanceStateDead.getInstance()) {
                    return;
                }
                Thread.sleep(INTERVAL);
            } catch (InterruptedException e) {
                System.out.println("Error: health check thread for instance " + instance.getId() + " was interrupted");
            }
        }
    }
}
