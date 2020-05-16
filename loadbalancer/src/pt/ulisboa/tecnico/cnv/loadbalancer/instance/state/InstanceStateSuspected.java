package pt.ulisboa.tecnico.cnv.loadbalancer.instance.state;

import pt.ulisboa.tecnico.cnv.loadbalancer.instance.Instance;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.RequestQueue;

public class InstanceStateSuspected extends InstanceState {
    private static InstanceStateSuspected instance = new InstanceStateSuspected();

    @Override
    public void newState(Instance instance) {
        RequestQueue.notifyQueue();
    }

    @Override
    public String name() {
        return "suspected";
    }

    public static InstanceState getInstance() {
        return instance;
    }
}

