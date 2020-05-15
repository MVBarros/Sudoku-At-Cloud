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
    private static InstanceManager instance;

    private final Map<String, Instance> instances = new ConcurrentHashMap<>();
    private final Executor healthCheckExecutor = Executors.newCachedThreadPool();
    private final Byte monitor = 0;

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
                notifyMonitor();
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

    public void sendRequest(SudokuRequest request) {
        Instance instance;
        synchronized (instances) {
            instance = getBestInstance();
            request.setInstance(instance);
        }

        HttpURLConnection connection = instance.getSudokuRequestConn(request.getParameters());
        request.sendRequest(connection);
    }

    private Instance getBestInstance() {
        Instance instance = searchBestInstance();
        while (instance == null) {
            waitMonitor();
            instance = searchBestInstance();
        }
        return instance;
    }

    private void waitMonitor() {
        synchronized (monitor) {
            try {
                monitor.wait();
            } catch (InterruptedException e) {
                //Do nothing
                System.out.println("Error: Thread was interrupted waiting on lock");
            }
        }
    }

    public void notifyMonitor() {
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }

    private Instance searchBestInstance() {
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
