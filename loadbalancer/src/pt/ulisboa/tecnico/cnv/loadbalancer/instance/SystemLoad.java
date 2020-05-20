package pt.ulisboa.tecnico.cnv.loadbalancer.instance;

public class SystemLoad {
    private final long avgNumberRequests;
    private final long  systemLoad;

    public SystemLoad(long avgNumberRequests, long systemLoad) {
        this.avgNumberRequests = avgNumberRequests;
        this.systemLoad = systemLoad;
    }

    public long getAvgNumberRequests() {
        return avgNumberRequests;
    }

    public long getSystemLoad() {
        return systemLoad;
    }
}
