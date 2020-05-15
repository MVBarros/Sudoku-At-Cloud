package pt.ulisboa.tecnico.cnv.loadbalancer.instance;

import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuParameters;
import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuRequest;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.HealthCheckTask;

import com.sun.net.httpserver.HttpExchange;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class InstanceManager {
    private static InstanceManager instance;

    private final Map<String, Instance> instances = new ConcurrentHashMap<>();
    private final Executor healthCheckExecutor = Executors.newCachedThreadPool();
    private final Set<SudokuParameters> requestQueue = Collections.synchronizedSet(new HashSet<SudokuParameters>());

    private InstanceManager() {
    }

    public static InstanceManager getInstance() {
        if (instance == null) {
            instance = new InstanceManager();
        }
        return instance;
    }

    public long getTotalLoad() {
        long totalLoad = 0;
        synchronized (instances) {
            for (Instance instance : instances.values()) {
                totalLoad += instance.getLoad();
            }
        }
        return totalLoad;
    }

    public void addInstance(String address, String id) throws MalformedURLException {
        synchronized (instances) {
            if (!instances.containsKey(id)) {
                Instance instance = new Instance(address, id);
                instances.put(instance.getId(), instance);
                healthCheckExecutor.execute(new HealthCheckTask(instance));
                notifyWaitingRequests();
            }
        }
    }

    public void removeInstance(String id) {
        Instance instance = instances.remove(id);
        if (instance != null) {
            for (SudokuRequest request : instance.getRequests()) {
                sendRequest(request.getParameters());
            }
        }
    }



    public void sendRequest(SudokuParameters parameters) {
        Instance instance;
        synchronized (instances) { //Otherwise we might add a request to the queue just as it is getting evicted
            instance = getBestInstance();
            if (instance == null) {
                //No instance currently available, wait
                requestQueue.add(parameters);
                return;
            }
        }
        SudokuRequest request = new SudokuRequest(parameters, instance);
        HttpURLConnection connection = instance.getSudokuRequestConn(request.getParameters());
        request.sendRequest(connection);
    }


    public void notifyWaitingRequests() {
        synchronized (instances) {
            for (SudokuParameters parameters : requestQueue) {
                sendRequest(parameters);
            }
            requestQueue.clear();
        }
    }

    private Instance getBestInstance() {
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

    public String getInstanceToRemove() {
        //TODO - Make actual selection of instance to remove
        return instances.values().iterator().next().getId();
    }
}
