package metrics.tools;

public class StatsBFS implements Stats {

    private static final double SLOPE = 196.60388922;
    private static final double INTERCEPT = -41020.3661539;
    public static final String BFS_TABLE_NAME = "BFS-Stats";

    private long methodCount = 0;

    public long getCost() {
        return (long) (SLOPE * methodCount + INTERCEPT);
    }

    @Override
    public String getTableName() {
        return BFS_TABLE_NAME;
    }

    void incrMethodCount() {
        this.methodCount++;
    }
}
