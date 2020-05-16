package pt.ulisboa.tecnico.cnv.loadbalancer.instance;

import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuParameters;
import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuRequest;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.QueueRemovalTask;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.ThreadManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Requests end up here when there are no instances available
 **/
public class RequestQueue {
    private static final int NUMBER_REMOVAL_THREADS = 1;
    private static final BlockingQueue<SudokuParameters> queue = new LinkedBlockingQueue<>(); //Pretty much an infinite queue
    private static final ReadWriteLock queueLock = new ReentrantReadWriteLock();
    private static final Condition noInstanceCondition = queueLock.writeLock().newCondition();

    //On class load create thread to remove entries from queue
    static {
        for (int i = 0; i < NUMBER_REMOVAL_THREADS; i++) {
            ThreadManager.execute(new QueueRemovalTask());
        }
    }

    private RequestQueue() {
    }

    public static void addToQueue(SudokuParameters parameters) {
        System.out.println("Request " + parameters + " was added to queue");
        queue.add(parameters);
    }

    public static SudokuRequest removeFromQueue() throws InterruptedException {
        SudokuParameters parameters = queue.take();
        Instance instance;
        queueLock.readLock().lock();
        while ((instance = InstanceManager.getBestInstance()) == null) {
            //Upgrade read lock to write lock (Java does not support out of the box lock upgrade)
            queueLock.readLock().unlock();
            queueLock.writeLock().lock();
            noInstanceCondition.await();
            queueLock.writeLock().unlock();
            queueLock.readLock().lock();
        }
        queueLock.readLock().unlock();
        return new SudokuRequest(parameters, instance);
    }

    public static void notifyQueue() {
        queueLock.writeLock().lock();
        System.out.println("Signalling queue of new instance");
        noInstanceCondition.signalAll();
        queueLock.writeLock().unlock();
    }
}
