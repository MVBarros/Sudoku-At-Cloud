package pt.ulisboa.tecnico.cnv.loadbalancer.instance.state;

import pt.ulisboa.tecnico.cnv.loadbalancer.instance.Instance;

public class InstanceStateSuspected extends InstanceState {
    private static InstanceStateSuspected instance = new InstanceStateSuspected();

    @Override
    public void newState(Instance instance) {
    }

    @Override
    public String name() {
        return "suspected";
    }

    public static InstanceState getInstance() {
        return instance;
    }
}

