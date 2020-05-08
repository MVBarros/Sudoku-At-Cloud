package metrics.tools;

public class StatsCP implements Stats {

    private static final double SLOPE = 293.64997239;
    //private static final double INTERCEPT = -1532.31767553;
    public static final String CP_TABLE_NAME = "CP-Stats";

    private long newCount = 0;

    public long getCost() {
        return (long) (SLOPE * newCount);
    }

    void incrNewCount() {
        this.newCount++;
    }

    @Override
    public String getTableName() {
        return CP_TABLE_NAME;
    }
}
