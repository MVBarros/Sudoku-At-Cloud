import org.json.JSONObject;

public class Stats {
    private long methodCount = 0;
    private long basicBlockCount = 0;
    private long instructionCount = 0;

    private long newCount = 0;
    private long newArrayCount = 0;
    private long aNewArrayCount = 0;
    private long multiANewArrayCount = 0;

    private long loadCount = 0;
    private long storeCount = 0;
    private long fieldLoadCount = 0;
    private long fieldStoreCount = 0;

    private long branchCount = 0;

    public long getMethodCount() {
        return methodCount;
    }

    public void incrMethodCount() {
        this.methodCount++;
    }

    public long getBasicBlockCount() {
        return basicBlockCount;
    }

    public void incrBasicBlockCount() {
        this.basicBlockCount++;
    }

    public long getInstructionCount() {
        return instructionCount;
    }

    public void incrInstructionCount(long count) {
        this.instructionCount += count;
    }

    public long getNewCount() {
        return newCount;
    }

    public void incrNewCount() {
        this.newCount++;
    }

    public long getNewArrayCount() {
        return newArrayCount;
    }

    public void incrNewArrayCount() {
        this.newArrayCount++;
    }

    public long getANewArrayCount() {
        return aNewArrayCount;
    }

    public void incrANewArrayCount() {
        this.aNewArrayCount++;
    }

    public long getMultiANewArrayCount() {
        return multiANewArrayCount;
    }

    public void incrMultiANewArrayCount() {
        this.multiANewArrayCount++;
    }

    public long getLoadCount() {
        return loadCount;
    }

    public void incrLoadCount() {
        this.loadCount++;
    }

    public long getStoreCount() {
        return storeCount;
    }

    public void incrStoreCount() {
        this.storeCount++;
    }

    public long getFieldLoadCount() {
        return fieldLoadCount;
    }

    public void incrFieldLoadCount() {
        this.fieldLoadCount++;
    }

    public long getFieldStoreCount() {
        return fieldStoreCount;
    }

    public void incrFieldStoreCount() {
        this.fieldStoreCount++;
    }


    public long getBranchCount() {
        return branchCount;
    }

    public void incrBranchCount() {
        this.branchCount++;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("Method Count", getMethodCount());
        obj.put("Basic Block Count", getBasicBlockCount());
        obj.put("Basic Instruction Count", getInstructionCount());
        obj.put("A New Array Count", getANewArrayCount());
        obj.put("New Array Count", getNewArrayCount());
        obj.put("New Count", getNewCount());
        obj.put("Multi New Array Count", getMultiANewArrayCount());
        obj.put("Load Count", getLoadCount());
        obj.put("Store Count", getStoreCount());
        obj.put("Field Load Count", getFieldLoadCount());
        obj.put("Field Store Count", getFieldStoreCount());
        obj.put("Branch Count", getBranchCount());
        return obj;
    }

}
