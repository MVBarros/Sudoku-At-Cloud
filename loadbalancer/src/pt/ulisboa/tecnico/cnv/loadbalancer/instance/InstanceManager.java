package pt.ulisboa.tecnico.cnv.loadbalancer.instance;

import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuRequest;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.HealthCheckTask;

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
    private final Set<SudokuRequest> requestQueue = Collections.synchronizedSet(new HashSet<SudokuRequest>());
    private InstanceManager() {
    }

    public static InstanceManager getInstance() {
        if (instance == null) {
            instance = new InstanceManager();
        }
        return instance;
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
                sendRequest(request);
            }
        }
    }

    public boolean sendRequest(SudokuRequest request) {
        Instance instance;
        synchronized (instances) { //Otherwise we might add a request to the queue just as it is getting evicted
            instance = getBestInstance();
            if (instance == null) {
                //No instance currently available, wait
                requestQueue.add(request);
                return false;
            } else {
                request.setInstance(instance);
            }
        }
        HttpURLConnection connection = instance.getSudokuRequestConn(request.getParameters());
        request.sendRequest(connection);
        return true;
    }


    public void notifyWaitingRequests() {
        synchronized (instances) {
            for (SudokuRequest request : requestQueue) {
                sendRequest(request);
            }
            //Clean queue by seeing which requests where successful and removing them
            List<SudokuRequest> sentRequests = new ArrayList<>();
            for (SudokuRequest request : requestQueue) {
                if (request.isFinised()) {
                    sentRequests.add(request);
                }
            }
            requestQueue.removeAll(sentRequests);
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
}
