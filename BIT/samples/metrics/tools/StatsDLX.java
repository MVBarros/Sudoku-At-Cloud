package metrics.tools;

public class StatsDLX implements Stats {

    private static final double SLOPE = 2608062.05699572;
    private static final double INTERCEPT = -282459324.512;
    public static final String DLX_TABLE_NAME = "DLX-Stats";

    private long newArrayCount = 0;

    public long getCost() {
        return (long) (SLOPE * newArrayCount + INTERCEPT);
    }

    void incrNewArrayCount() {
        this.newArrayCount++;
    }

    @Override
    public String getTableName() {
        return DLX_TABLE_NAME;
    }
}
