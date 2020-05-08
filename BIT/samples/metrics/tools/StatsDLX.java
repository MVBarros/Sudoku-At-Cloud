package metrics.tools;

public class StatsDLX implements Stats {

    private long newArrayCount = 0;

    public long getNewArrayCount() {
        return newArrayCount;
    }

    public void incrNewArrayCount() {
        this.newArrayCount++;
    }

}
