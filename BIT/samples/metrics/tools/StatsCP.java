package metrics.tools;

public class StatsCP implements Stats {

    private static final double SLOPE = 294.42221265;
    private static final double INTERCEPT = -1532.31767553;
    public static final String CP_TABLE_NAME = "CP-Stats";

    private long newCount = 0;

    public long getCost() {
        return (long) (SLOPE * newCount + INTERCEPT);
    }

    void incrNewCount() {
        this.newCount++;
    }

    @Override
    public String getTableName() {
        return CP_TABLE_NAME;
    }
}
