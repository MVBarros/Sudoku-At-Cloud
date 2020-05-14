package pt.ulisboa.tecnico.cnv.loadbalancer.task;

import pt.ulisboa.tecnico.cnv.loadbalancer.instance.Instance;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.InstanceManager;


public class HealthCheckTask implements Runnable {
    private static final int INTERVAL = 10000; //10 seconds
    private static final int GRACE_PERIOD = 30000; //30 seconds
    private static final int UNHEALTHY_THRESHOLD = 3; //3 timeouts before declaring instance as dead


    private final Instance instance;
    private int failureCounter;

    public HealthCheckTask(Instance instance) {
        this.instance = instance;
        this.failureCounter = 0;
    }

    @Override
    public void run() {
        waitForGracePeriod();
        while (true) {
            try {
                failureCounter = instance.healthCheck() ? 0 : failureCounter + 1;
                if (failureCounter == UNHEALTHY_THRESHOLD) {
                    System.out.println("Instance " + instance.getAddress() + " has been marked dead");
                    InstanceManager.getInstance().removeInstance(instance.getAddress());
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
