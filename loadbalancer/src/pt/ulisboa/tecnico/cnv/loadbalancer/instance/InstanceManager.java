package pt.ulisboa.tecnico.cnv.loadbalancer.instance;

import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuRequest;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.HealthCheckTask;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class InstanceManager {
    private static InstanceManager instanceManager;

    private final Map<String, Instance> instances = new ConcurrentHashMap<>();
    private final Executor healthCheckExecutor = Executors.newCachedThreadPool();

    private InstanceManager() {
    }

    public static InstanceManager getInstance() {
        if (instanceManager == null) {
            instanceManager = new InstanceManager();
        }
        return instanceManager;
    }

    public void addInstance(String address) throws MalformedURLException {
        synchronized (instances) {
            if (!instances.containsKey(address)) {
                Instance instance = new Instance(address);
                instances.put(instance.getAddress(), instance);
                healthCheckExecutor.execute(new HealthCheckTask(instance));
            }
        }
    }

    public void removeInstance(String address) {
        Instance instance = instances.remove(address);
        if (instance != null) {
            for (SudokuRequest request : instance.getRequests()) {
                sendRequest(request);
            }
        }
    }

    public void sendRequest(SudokuRequest request) {
        Instance instance;
        synchronized (instances) {
            instance = getBestInstance();
            request.setInstance(instance);
        }

        HttpURLConnection connection = instance.getSudokuRequestConn(request.getParameters());
        request.sendRequest(connection);
    }

    //FIXME ask if we can assume at least one instance at a time
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
