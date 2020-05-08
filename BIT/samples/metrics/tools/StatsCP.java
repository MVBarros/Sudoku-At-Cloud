package metrics.tools;

public class StatsCP implements Stats {

    private long newCount = 0;

    public long getNewCount() {
        return newCount;
    }

    public void incrNewCount() {
        this.newCount++;
    }


}
