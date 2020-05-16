package pt.ulisboa.tecnico.cnv.loadbalancer.instance.state;

import pt.ulisboa.tecnico.cnv.loadbalancer.instance.Instance;

public abstract class InstanceState {
    protected InstanceState() {
    }

    @Override
    public boolean equals(Object other) {
        return other.getClass() == this.getClass();
    }

    public abstract void newState(Instance instance);

    public abstract String name();
}
