package pt.ulisboa.tecnico.cnv.loadbalancer.instance;

import pt.ulisboa.tecnico.cnv.loadbalancer.task.HealthCheckTask;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.ThreadManager;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InstanceManager {

    private static final long TIME_INTERVAL_TOLERANCE = 5 * 60 * 1000;

    private static final Map<String, Instance> instances = new ConcurrentHashMap<>();
    private static final Map<String, HealthCheckTask> healthCheckThreads = new ConcurrentHashMap<>();

    private InstanceManager() {
    }

    public static long getSystemLoad() {
        synchronized (instances) {
            long totalLoad = 0;
            for (Instance instance : instances.values()) {
                totalLoad += instance.getLoad();
            }
            return totalLoad / (instances.size() == 0 ? 1 : instances.size()); //Just in case, should never happen
        }
    }

    public static void addInstance(String address, String id) throws MalformedURLException {
        Instance instance;
        boolean isNew;
        synchronized (instances) {
            isNew = !instances.containsKey(id);
            if (isNew) {
                instance = new Instance(address, id);
                instances.put(id, instance);
                HealthCheckTask task = new HealthCheckTask(instance);
                healthCheckThreads.put(instance.getId(), task);
                ThreadManager.execute(task);
            }
        }
    }


    public static Instance removeInstance(String id) {
        synchronized (instances) {
            HealthCheckTask task = healthCheckThreads.remove(id);
            if (task != null) {
                task.interrupt();
            } else {
                System.out.println("Error: Removing health check for instance " + id + " that does not exist");
            }
            return instances.remove(id);
        }
    }


    public static Instance getBestInstance() {
        Instance bestInstance = null;
        for (Instance instance : instances.values()) {
            if (instance.isHealthy()) {
                if (bestInstance == null || bestInstance.getLoad() > instance.getLoad()) {
                    bestInstance = instance;
                }
            }
        }
        return bestInstance;
    }

    public static Instance getInstanceToRemove() {
        synchronized (instances) {
            if (instances.size() == 0) {
                return null; //Just in case
            }

            Instance best = null;
            //Chose instance closer to completing requests (since we will be waiting for it to finish)
            for (Instance instance : instances.values()) {
                if (best == null || best.estimateCompletionTime() > instance.estimateCompletionTime()) {
                    best = instance;
                }
            }

            //prioritize older instances
            for (Instance instance : instances.values()) {
                if (best.estimateCompletionTime() + TIME_INTERVAL_TOLERANCE >= instance.estimateCompletionTime()
                        && best.getUpTime() < instance.getUpTime()) {
                    best = instance;
                }
            }
            removeInstance(best.getId());
            return best;
        }
    }
}
