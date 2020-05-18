package pt.ulisboa.tecnico.cnv.loadbalancer.instance;

import pt.ulisboa.tecnico.cnv.loadbalancer.task.HealthCheckTask;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.ThreadManager;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InstanceManager {

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
            return totalLoad / instances.size();
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

    public static String getInstanceToRemove() {
        //TODO FIX 0 INSTANCES
        //TODO - Make actual selection of instance to remove
        return instances.values().iterator().next().getId();
    }
}
