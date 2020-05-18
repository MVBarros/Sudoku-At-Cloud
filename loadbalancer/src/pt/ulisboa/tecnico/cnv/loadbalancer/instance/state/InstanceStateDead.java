package pt.ulisboa.tecnico.cnv.loadbalancer.instance.state;

import pt.ulisboa.tecnico.cnv.autoscaler.EC2FrontEnd;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.Instance;

public class InstanceStateDead extends InstanceState {
    private static InstanceStateDead instance = new InstanceStateDead();

    @Override
    public void newState(Instance instance) {
        EC2FrontEnd.terminateInstance(instance.getId());
    }

    @Override
    public String name() {
        return "dead";
    }

    public static InstanceState getInstance() {
        return instance;
    }
}
