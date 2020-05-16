package pt.ulisboa.tecnico.cnv.loadbalancer.task;

import pt.ulisboa.tecnico.cnv.loadbalancer.instance.RequestQueue;
import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuRequest;

public class QueueRemovalTask implements Runnable {
    @Override
    public void run() {
        while(true) {
            try {
                SudokuRequest request = RequestQueue.removeFromQueue();
                ThreadManager.execute(request);
            } catch (InterruptedException e) {
                System.out.println("Error: Queue Removal task runnin on thread "
                        + Thread.currentThread().getName() + " was interrupted");
            }
        }
    }
}
