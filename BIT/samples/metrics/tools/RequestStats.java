package metrics.tools;

public class RequestStats {
    private static final double BFS_INTERCEPT = 489035592.21921813;
    private static final double BFS_SLOPE_METHOD = 617084.43306421;
    private static final double BFS_SLOPE_N1 = -41030543.22194641;
    private static final double BFS_SLOPE_N2 = -41030543.22194641;
    private static final double BFS_SLOPE_UN = -72864.72088934;

    private static final double CP_INTERCEPT = -26810917.901337147;
    private static final double CP_SLOPE_NEW = 2058642.81;
    private static final double CP_SLOPE_N1 = -1026671.74;
    private static final double CP_SLOPE_N2 = -1026671.74;
    private static final double CP_SLOPE_UN = 184.759923;

    private static final double DLX_INTERCEPT = 2943278992.671898;
    private static final double DLX_SLOPE_METHOD = 310610.877;
    private static final double DLX_SLOPE_N1 = -182842646;
    private static final double DLX_SLOPE_N2 = -182842646;
    private static final double DLX_SLOPE_UN = -651744.764;

    private long methodCount = 0;
    private long newCount = 0;

    void incrMethodCount() {
        this.methodCount++;
    }

    void incrNewCount() {
        this.newCount++;
    }

    public long getBFSCost(int n1, int n2, int un) {
        return (long) Math.abs(n1 * BFS_SLOPE_N1 + n2 * BFS_SLOPE_N2 + un * BFS_SLOPE_UN + this.methodCount * BFS_SLOPE_METHOD + BFS_INTERCEPT);
    }

    public long getCPCost(int n1, int n2, int un) {
        return (long) Math.abs(n1 * CP_SLOPE_N1 + n2 * CP_SLOPE_N2 + un * CP_SLOPE_UN + this.newCount * CP_SLOPE_NEW + CP_INTERCEPT);
    }

    public long getDLXCost(int n1, int n2, int un) {
        return (long) Math.abs(n1 * DLX_SLOPE_N1 + n2 * DLX_SLOPE_N2 + un * DLX_SLOPE_UN + this.methodCount * DLX_SLOPE_METHOD + DLX_INTERCEPT);
    }
}
