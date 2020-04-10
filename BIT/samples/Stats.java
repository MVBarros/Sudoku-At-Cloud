import org.json.JSONObject;
import org.json.JSONArray;
public class Stats {
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

    private int branchCount = 0;

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


    public int getBranchCount() {
        return branchCount;
    }

    public void incrBranchCount() {
        this.branchCount++;
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
        obj.put("Branch Count", getBranchCount());
        return obj;
    }

}
