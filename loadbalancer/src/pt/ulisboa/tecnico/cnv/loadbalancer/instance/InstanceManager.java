package pt.ulisboa.tecnico.cnv.loadbalancer.instance;

import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuRequest;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.HealthCheckTask;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class InstanceManager {
    private static InstanceManager instance;

    private final Map<String, Instance> instances = new ConcurrentHashMap<>();
    private final Executor healthCheckExecutor = Executors.newCachedThreadPool();
    private final List<SudokuRequest> requestQueue = Collections.synchronizedList(new ArrayList<SudokuRequest>());
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
        synchronized (requestQueue) { //Otherwise we might add a request to the queue just as it is getting evicted
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
        synchronized (requestQueue) {
            List<SudokuRequest> sentRequests = new ArrayList<>();
            for (SudokuRequest request : requestQueue) {
                if (sendRequest(request)) {
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
