package metrics.tools;

import org.json.JSONObject;

public class StatsBFS implements Stats {

    private long methodCount = 0;
    private long storeCount = 0;
    private long fieldLoadCount = 0;

    public long getMethodCount() {
        return methodCount;
    }

    public void incrMethodCount() {
        this.methodCount++;
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

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("Method Count", getMethodCount());
        obj.put("Store Count", getStoreCount());
        obj.put("Field Load Count", getFieldLoadCount());
        return obj;
    }
}
