package pt.ulisboa.tecnico.cnv.loadbalancer.instance.state;

import pt.ulisboa.tecnico.cnv.autoscaler.AutoScaler;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.Instance;

public class InstanceStateDead extends InstanceState {
    private static InstanceStateDead instance = new InstanceStateDead();

    @Override
    public void newState(Instance instance) {
        AutoScaler.terminateInstance(instance.getId());
    }

    @Override
    public String name() {
        return "dead";
    }

    public static InstanceState getInstance() {
        return instance;
    }
}
