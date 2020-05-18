package pt.ulisboa.tecnico.cnv.autoscaler.task;

import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import pt.ulisboa.tecnico.cnv.autoscaler.AutoScaler;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.Instance;

public class RemoveInstanceTask implements Runnable {

    private Instance removedInstance;
    public RemoveInstanceTask(Instance instance) { this.removedInstance = instance;}

    @Override
    public void run() {
        System.out.println("Removed instance " + removedInstance.getId());

        //Wait for instance to finish it's job
        while(removedInstance.getRequests().size() > 0){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
        termInstanceReq.withInstanceIds(removedInstance.getId());
        AutoScaler.getEc2().terminateInstances(termInstanceReq);

    }
}
