package metrics.tools;

import org.json.JSONObject;

public class StatsDLX implements Stats {


    private long newArrayCount = 0;

    public long getNewArrayCount() {
        return newArrayCount;
    }

    public void incrNewArrayCount() {
        this.newArrayCount++;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("New Array Count", getNewArrayCount());
        return obj;
    }
}
