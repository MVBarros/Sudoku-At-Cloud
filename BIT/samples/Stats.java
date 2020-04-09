

public class Stats {
    private static Integer branchCount = 0;

    private int dyn_method_count = 0;
    private int dyn_bb_count = 0;
    private int dyn_instr_count = 0;

    private int newcount = 0;
    private int newarraycount = 0;
    private int anewarraycount = 0;
    private int multianewarraycount = 0;

    private int loadcount = 0;
    private int storecount = 0;
    private int fieldloadcount = 0;
    private int fieldstorecount = 0;

    private StatisticsBranch[] branch_info = null;
    private int branch_number = 0;
    private int branch_pc = 0;
    private static String branch_class_name = null;
    private String branch_method_name = null;

    public int getDyn_method_count() {
        return dyn_method_count;
    }

    public void incrDyn_method_count(int dyn_method_count) {
        this.dyn_method_count += dyn_method_count;
    }

    public int getDyn_bb_count() {
        return dyn_bb_count;
    }

    public void incrDyn_bb_count(int dyn_bb_count) {
        this.dyn_bb_count += dyn_bb_count;
    }

    public int getDyn_instr_count() {
        return dyn_instr_count;
    }

    public void incrDyn_instr_count(int dyn_instr_count) {
        this.dyn_instr_count += dyn_instr_count;
    }

    public int getNewcount() {
        return newcount;
    }

    public void incrNewcount(int newcount) {
        this.newcount += newcount;
    }

    public int getNewarraycount() {
        return newarraycount;
    }

    public void incrNewarraycount(int newarraycount) {
        this.newarraycount += newarraycount;
    }

    public int getAnewarraycount() {
        return anewarraycount;
    }

    public void incrAnewarraycount(int anewarraycount) {
        this.anewarraycount += anewarraycount;
    }

    public int getMultianewarraycount() {
        return multianewarraycount;
    }

    public void incrMultianewarraycount(int multianewarraycount) {
        this.multianewarraycount += multianewarraycount;
    }

    public int getLoadcount() {
        return loadcount;
    }

    public void incrLoadcount(int loadcount) {
        this.loadcount += loadcount;
    }

    public int getStorecount() {
        return storecount;
    }

    public void incrStorecount(int storecount) {
        this.storecount += storecount;
    }

    public int getFieldloadcount() {
        return fieldloadcount;
    }

    public void incrFieldloadcount(int fieldloadcount) {
        this.fieldloadcount += fieldloadcount;
    }

    public int getFieldstorecount() {
        return fieldstorecount;
    }

    public void incrFieldstorecount(int fieldstorecount) {
        this.fieldstorecount += fieldstorecount;
    }

    public StatisticsBranch[] getBranch_info() {
        if (this.branch_info == null) {
            setBranch_info(new StatisticsBranch[Stats.getBranchCount()]);
        }
        return this.branch_info;
    }

    public void setBranch_info(StatisticsBranch[] branch_info) {
        this.branch_info = branch_info;
    }

    public int getBranch_number() {
        return branch_number;
    }

    public void setBranch_number(int branch_number) {
        this.branch_number = branch_number;
    }

    public int getBranch_pc() {
        return branch_pc;
    }

    public void setBranch_pc(int branch_pc) {
        this.branch_pc = branch_pc;
    }

    public static String getBranch_class_name() {
        return Stats.branch_class_name;
    }

    public static void setBranch_class_name(String branch_class_name) {
        Stats.branch_class_name = branch_class_name;
    }

    public String getBranch_method_name() {
        return branch_method_name;
    }

    public void setBranch_method_name(String branch_method_name) {
        this.branch_method_name = branch_method_name;
    }

    public static int getBranchCount() {
        return branchCount;
    }

    public static void setBranchCount(int n) {
        Stats.branchCount = n;
    }

}
