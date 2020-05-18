package pt.ulisboa.tecnico.cnv.autoscaler.task;

import pt.ulisboa.tecnico.cnv.autoscaler.EC2FrontEnd;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.Instance;

public class RemoveInstanceTask implements Runnable {
    private static final int WAIT_TIME = 2000;
    private Instance instance;

    public RemoveInstanceTask(Instance instance) {
        this.instance = instance;
    }

    @Override
    public void run() {
        System.out.println("Removed instance " + instance.getId());

        //Wait for instance to finish it's job
        while (instance.getRequests().size() > 0) {
            try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                System.out.println("Error: Thread was interrupted while waiting for removed instance " + instance.getId() +" to finish its job");
            }
        }
        EC2FrontEnd.terminateInstance(instance);
    }
}
