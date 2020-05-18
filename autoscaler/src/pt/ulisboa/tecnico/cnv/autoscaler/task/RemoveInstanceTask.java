package pt.ulisboa.tecnico.cnv.autoscaler.task;

import pt.ulisboa.tecnico.cnv.autoscaler.AutoScaler;

public class RemoveInstanceTask implements Runnable {

    private String instanceId;

    public RemoveInstanceTask(String instanceId) { this.instanceId = instanceId;}

    @Override
    public void run() {
        AutoScaler.terminateInstance(this.instanceId);
    }
}
