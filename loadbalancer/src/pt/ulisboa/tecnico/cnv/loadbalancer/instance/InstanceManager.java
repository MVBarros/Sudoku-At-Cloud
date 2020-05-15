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
    private static final Set<SudokuParameters> waitingQueue = Collections.synchronizedSet(new HashSet<SudokuParameters>());

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

    public static void addInstance(Instance instance) {
        synchronized (instances) {
            if (!instances.containsKey(instance.getId())) {
                instances.put(instance.getId(), instance);
                ThreadManager.execute(new HealthCheckTask(instance));
                repeatWaitingRequests();
            }
        }
    }

    public static void addInstance(String address, String id) throws MalformedURLException {
        addInstance(new Instance(address, id));
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
        synchronized (instances) {
            SudokuRequest request = createRequest(parameters);
            if (request == null) {
                waitingQueue.add(parameters);
            } else {
                ThreadManager.execute(request);
            }
        }
    }

    private static SudokuRequest createRequest(SudokuParameters parameters) {
        Instance instance = getBestInstance();
        return instance == null ? null : new SudokuRequest(parameters, instance);
    }

    public static void repeatWaitingRequests() {
        synchronized (instances) {
            Set<SudokuParameters> queueCopy = new HashSet<>(waitingQueue);
            waitingQueue.clear();
            for (SudokuParameters parameters : queueCopy) {
                sendRequest(parameters);
            }
        }
    }

    private static Instance getBestInstance() {
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

    public static String getInstanceToRemove() {
        //TODO - Make actual selection of instance to remove
        return instances.values().iterator().next().getId();
    }
}
