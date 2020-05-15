package pt.ulisboa.tecnico.cnv.loadbalancer.instance;

import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuParameters;
import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuRequest;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.HealthCheckTask;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.ThreadManager;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InstanceManager {

    private static final Map<String, Instance> instances = new ConcurrentHashMap<>();
    private static final Set<SudokuParameters> requestQueue = Collections.synchronizedSet(new HashSet<SudokuParameters>());

    private InstanceManager() {
    }


    public static long getTotalLoad() {
        long totalLoad = 0;
        synchronized (instances) {
            for (Instance instance : instances.values()) {
                totalLoad += instance.getLoad();
            }
        }
        return totalLoad;
    }

    public static void addInstance(String address, String id) throws MalformedURLException {
        synchronized (instances) {
            if (!instances.containsKey(id)) {
                Instance instance = new Instance(address, id);
                instances.put(instance.getId(), instance);
                ThreadManager.execute(new HealthCheckTask(instance));
                notifyWaitingRequests();
            }
        }
    }

    public static void removeInstance(String id) {
        Instance instance = instances.remove(id);
        if (instance != null) {
            for (SudokuRequest request : instance.getRequests()) {
                sendRequest(request.getParameters());
            }
        }
    }


    public static void sendRequest(SudokuParameters parameters) {
        Instance instance;
        synchronized (instances) { //Otherwise we might add a request to the queue just as it is getting evicted
            instance = getBestInstance();
            if (instance == null) {
                //No instance currently available, wait
                requestQueue.add(parameters);
                return;
            }
        }
        new SudokuRequest(parameters, instance).run();
    }


    public static void notifyWaitingRequests() {
        synchronized (instances) {
            for (SudokuParameters parameters : requestQueue) {
                sendRequest(parameters);
            }
            requestQueue.clear();
        }
    }

    private static Instance getBestInstance() {
        synchronized (instances) {
            Instance bestInstance = null;
            for (Instance instance : instances.values()) {
                if (instance.getState() == Instance.InstanceState.HEALTHY) {
                    if (bestInstance == null || bestInstance.getLoad() > instance.getLoad()) {
                        bestInstance = instance;
                    }
                }
            }
            return bestInstance;
        }
    }

    public static String getInstanceToRemove() {
        //TODO - Make actual selection of instance to remove
        return instances.values().iterator().next().getId();
    }
}
