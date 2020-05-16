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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class InstanceManager {

    private static final Map<String, Instance> instances = new ConcurrentHashMap<>();
    private static final Set<SudokuParameters> waitingQueue = Collections.synchronizedSet(new HashSet<SudokuParameters>());

    private static final ReadWriteLock instancesLock = new ReentrantReadWriteLock(); //Allows multiple threads to read together but isolates writers from readers

    private InstanceManager() {}

    public static long getTotalLoad() {
        long totalLoad = 0;
        instancesLock.readLock().lock();
        for (Instance instance : instances.values()) {
            totalLoad += instance.getLoad();
        }
        instancesLock.readLock().unlock();
        return totalLoad;
    }

    private static void addInstance(Instance instance) {
        instancesLock.writeLock().lock();
        boolean containsInstance = instances.containsKey(instance.getId());
        if (!containsInstance) {
            instances.put(instance.getId(), instance);
        }
        instancesLock.writeLock().unlock();

        if (!containsInstance) {
            ThreadManager.execute(new HealthCheckTask(instance));
            repeatWaitingRequests();
        }
    }

    public static void addInstance(String address, String id) throws MalformedURLException {
        addInstance(new Instance(address, id));
    }

    public static void removeInstance(String id) {
        instancesLock.writeLock().lock();
        instances.remove(id);
        instancesLock.writeLock().unlock();
    }

    public static void sendRequest(SudokuParameters parameters) {
        synchronized (waitingQueue) { //Prevent adding to queue just after it was cleared and keeping request waiting
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
        Set<SudokuParameters> queueCopy;
        synchronized (waitingQueue) {
            //Don't let any thread add to queue while copying
            queueCopy = new HashSet<>(waitingQueue);
            waitingQueue.clear();
        }
        for (SudokuParameters parameters : queueCopy) {
            sendRequest(parameters);
        }
    }

    private static Instance getBestInstance() {
        Instance bestInstance = null;
        instancesLock.readLock().lock();
        for (Instance instance : instances.values()) {
            if (instance.getState() == Instance.InstanceState.HEALTHY) {
                if (bestInstance == null || bestInstance.getLoad() > instance.getLoad()) {
                    bestInstance = instance;
                }
            }
        }
        instancesLock.readLock().unlock();
        return bestInstance;
    }

    public static String getInstanceToRemove() {
        //TODO - Make actual selection of instance to remove
        return instances.values().iterator().next().getId();
    }
}
