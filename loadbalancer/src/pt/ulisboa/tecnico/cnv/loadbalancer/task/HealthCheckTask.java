package pt.ulisboa.tecnico.cnv.loadbalancer.task;

import pt.ulisboa.tecnico.cnv.loadbalancer.instance.Instance;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.InstanceManager;


public class HealthCheckTask implements Runnable {
    private static final int INTERVAL = 10000; //10 seconds
    private static final int GRACE_PERIOD = 30000; //30 seconds


    private final Instance instance;

    public HealthCheckTask(Instance instance) {
        this.instance = instance;
    }

    @Override
    public void run() {
        waitForGracePeriod();
        while (true) {
            try {
                if (instance.healthCheck()) {
                    instance.resetFailureCounter();
                } else {
                    instance.incrFailureCounter();
                }
                if (instance.getState() == Instance.InstanceState.DEAD) {
                    InstanceManager.getInstance().removeInstance(instance.getId());
                    return;
                }
                Thread.sleep(INTERVAL);
            } catch (InterruptedException e) {
                System.out.println("Error: health check thread for instance " + instance.getAddress() + " was interrupted");
            }
        }
    }

    private void waitForGracePeriod() {
        try {
            Thread.sleep(GRACE_PERIOD);
        } catch (InterruptedException e) {
            System.out.println("Error: health check thread for instance " + instance.getAddress() + " was interrupted");
        }
    }
}
