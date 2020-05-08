package metrics.tools;

public class StatsDLX implements Stats {

    private static final double SLOPE = 1895380.3541758;
    //private static final double INTERCEPT = -282459324.512;
    public static final String DLX_TABLE_NAME = "DLX-Stats";

    private long newArrayCount = 0;

    public long getCost() {
        return (long) (SLOPE * newArrayCount);
    }

    void incrNewArrayCount() {
        this.newArrayCount++;
    }

    @Override
    public String getTableName() {
        return DLX_TABLE_NAME;
    }
}
