package metrics.tools;

public class StatsBFS implements Stats {

    private long methodCount = 0;


    public long getMethodCount() {
        return methodCount;
    }

    public void incrMethodCount() {
        this.methodCount++;
    }
}
