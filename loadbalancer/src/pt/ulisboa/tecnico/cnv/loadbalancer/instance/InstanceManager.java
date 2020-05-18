package pt.ulisboa.tecnico.cnv.loadbalancer.instance;

import pt.ulisboa.tecnico.cnv.loadbalancer.task.HealthCheckTask;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.ThreadManager;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InstanceManager {

    private static final Map<String, Instance> instances = new ConcurrentHashMap<>();

    private InstanceManager() {
    }

    public static long getTotalLoad() {
        long totalLoad = 0;
        for (Instance instance : instances.values()) {
            totalLoad += instance.getLoad();
        }
        return totalLoad;
    }

    public static void addInstance(String address, String id) throws MalformedURLException {
        Instance instance = null;
        boolean isNew;
        synchronized (instances) {
            isNew = !instances.containsKey(id);
            if (isNew) {
                instance = new Instance(address, id);
                instances.put(id, instance);
            }
        }
        if (isNew) {
            ThreadManager.execute(new HealthCheckTask(instance));
        }
    }

    public static void removeInstance(String id) {
        instances.remove(id);
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
        //TODO - Make actual selection of instance to remove
        return instances.values().iterator().next().getId();
    }

    public static int getNumInstances() {
        int numInstances = 0;
        synchronized (instances){
            for(Instance instance : instances.values()){
                if(instance.isHealthy()){
                    numInstances++;
                }
            }
        }
        return numInstances;
    }
}
