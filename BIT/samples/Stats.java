import org.json.JSONObject;
import org.json.JSONArray;
public class Stats {
    private static Integer branchCount = 0;

    private int methodCount = 0;
    private int dynamicBasicBlockCount = 0;
    private int instructionCount = 0;

    private int newCount = 0;
    private int newArrayCount = 0;
    private int aNewArrayCount = 0;
    private int multiANewArrayCount = 0;

    private int loadCount = 0;
    private int storeCount = 0;
    private int fieldLoadCount = 0;
    private int fieldStoreCount = 0;

    private StatisticsBranch[] branchInfo = null;
    private int branchNumber = 0;
    private int branchPc = 0;
    private String branchClassName = null;
    private String branchMethodName = null;

    public int getMethodCount() {
        return methodCount;
    }

    public void incrMethodCount() {
        this.methodCount++;
    }

    public int getDynamicBasicBlockCount() {
        return dynamicBasicBlockCount;
    }

    public void incrBasicBlockCount() {
        this.dynamicBasicBlockCount++;
    }

    public int getInstructionCount() {
        return instructionCount;
    }

    public void incrInstructionCount(int instructionCount) {
        this.instructionCount += instructionCount;
    }

    public int getNewCount() {
        return newCount;
    }

    public void incrNewCount() {
        this.newCount++;
    }

    public int getNewArrayCount() {
        return newArrayCount;
    }

    public void incrNewArrayCount() {
        this.newArrayCount++;
    }

    public int getANewArrayCount() {
        return aNewArrayCount;
    }

    public void incrANewArrayCount() {
        this.aNewArrayCount++;
    }

    public int getMultiANewArrayCount() {
        return multiANewArrayCount;
    }

    public void incrMultiANewArrayCount() {
        this.multiANewArrayCount++;
    }

    public int getLoadCount() {
        return loadCount;
    }

    public void incrLoadCount() {
        this.loadCount++;
    }

    public int getStoreCount() {
        return storeCount;
    }

    public void incrStoreCount() {
        this.storeCount++;
    }

    public int getFieldLoadCount() {
        return fieldLoadCount;
    }

    public void incrFieldLoadCount() {
        this.fieldLoadCount++;
    }

    public int getFieldStoreCount() {
        return fieldStoreCount;
    }

    public void incrFieldStoreCount() {
        this.fieldStoreCount++;
    }

    public StatisticsBranch[] getBranchInfo() {
        if (this.branchInfo == null) {
            setBranchInfo(new StatisticsBranch[Stats.getBranchCount()]);
        }
        return this.branchInfo;
    }

    public void setBranchInfo(StatisticsBranch[] branchInfo) {
        this.branchInfo = branchInfo;
    }

    public int getBranchNumber() {
        return branchNumber;
    }

    public void setBranchNumber(int branchNumber) {
        this.branchNumber = branchNumber;
    }

    public int getBranchPc() {
        return branchPc;
    }

    public void setBranchPc(int branchPc) {
        this.branchPc = branchPc;
    }

    public String getBranchClassName() {
        return this.branchClassName;
    }

    public void setBranchClassName(String branchClassName) {
        this.branchClassName = branchClassName;
    }

    public String getBranchMethodName() {
        return branchMethodName;
    }

    public void setBranchMethodName(String branchMethodName) {
        this.branchMethodName = branchMethodName;
    }

    public static int getBranchCount() {
        return branchCount;
    }

    public static void setBranchCount(int n) {
        Stats.branchCount = n;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("Method Count", getMethodCount());
        obj.put("Basic Block Count", getDynamicBasicBlockCount());
        obj.put("Basic Instruction Count", getInstructionCount());
        obj.put("A New Array Count", getANewArrayCount());
        obj.put("New Array Count", getNewArrayCount());
        obj.put("New Count", getNewCount());
        obj.put("Multi New Array Count", getMultiANewArrayCount());
        obj.put("Load Count", getLoadCount());
        obj.put("Store Count", getStoreCount());
        obj.put("Field Load Count", getFieldLoadCount());
        obj.put("Field Store Count", getStoreCount());
        JSONArray array = new JSONArray();
        StatisticsBranch[] branches = getBranchInfo();
        for (StatisticsBranch branch: branches) {
            if (branch != null) {
                array.put(branch.toJSON());
            }
        }
        obj.put("Branches", array);
        return obj;
    }

}
