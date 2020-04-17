package metrics.tools;

import org.json.JSONObject;

public class StatsBFS implements Stats {
    private long methodCount = 0;

    public long getMethodCount() {
        return methodCount;
    }

    public void incrMethodCount() {
        this.methodCount++;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("Method Count", getMethodCount());
        return obj;
    }

}
