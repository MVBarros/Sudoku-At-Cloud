package pt.ulisboa.tecnico.cnv.loadbalancer.instance.state;

import pt.ulisboa.tecnico.cnv.loadbalancer.instance.Instance;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.RequestQueue;

public class InstanceStateHealthy extends InstanceState {
    private static InstanceStateHealthy instance = new InstanceStateHealthy();

    @Override
    public void newState(Instance instance) {
        RequestQueue.notifyQueue();
    }

    @Override
    public String name() {
        return "healthy";
    }

    public static InstanceState getInstance() {
        return instance;
    }
}
