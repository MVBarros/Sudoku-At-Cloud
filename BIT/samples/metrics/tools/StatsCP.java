package metrics.tools;

import org.json.JSONObject;

public class StatsCP implements Stats {
    private long methodCount = 0;
    private long newCount = 0;
    private long fieldLoadCount = 0;

    public long getMethodCount() {
        return methodCount;
    }

    public void incrMethodCount() {
        this.methodCount++;
    }

    public long getNewCount() { return newCount; }

    public void incrNewCount() { this.newCount++; }

    public long getFieldLoadCount() {
        return fieldLoadCount;
    }

    public void incrFieldLoadCount() {
        this.fieldLoadCount++;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("Method Count", getMethodCount());
        obj.put("New Count", getNewCount());
        obj.put("Field Load Count", getFieldLoadCount());
        return obj;
    }
}
