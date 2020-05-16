package pt.ulisboa.tecnico.cnv.loadbalancer.instance;

import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuParameters;
import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuRequest;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.QueueRemovalTask;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.ThreadManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Requests end up here when there are no instances available
 **/
public class RequestQueue {
    private static final int NUMBER_REMOVAL_THREADS = 1; //More than this won't be efective due to strict locking
    private static final BlockingQueue<SudokuParameters> queue = new LinkedBlockingQueue<>(); //Pretty much an infinite queue
    private static final ReentrantLock queueLock = new ReentrantLock();
    private static final Condition noInstanceCondition = queueLock.newCondition();
    //Wanted to have read write lock but java does not support lock upgrade

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
        try {
            SudokuParameters parameters = queue.take();
            Instance instance;
            queueLock.lock();
            while ((instance = InstanceManager.getBestInstance()) == null) {
                noInstanceCondition.await();
            }
            return new SudokuRequest(parameters, instance);
        } finally {
            if (queueLock.isHeldByCurrentThread()) {
                queueLock.unlock();
            }
        }
    }

    public static void notifyQueue() {
        queueLock.lock();
        System.out.println("Signalling queue of new instance");
        noInstanceCondition.signalAll();
        queueLock.unlock();
    }
}
