package metrics.tools;

import org.json.JSONObject;

public class StatsCP implements Stats {

    private long newCount = 0;

    public long getNewCount() {
        return newCount;
    }

    public void incrNewCount() {
        this.newCount++;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("New Count", getNewCount());
        return obj;
    }

}
