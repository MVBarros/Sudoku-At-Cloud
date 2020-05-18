package pt.ulisboa.tecnico.cnv.autoscaler.task;

import pt.ulisboa.tecnico.cnv.autoscaler.EC2FrontEnd;

import java.util.concurrent.CountDownLatch;

public class CreateInstanceTask implements Runnable {


    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void run() {
        EC2FrontEnd.newInstance();
        latch.countDown();
    }

    public void waitFinish() throws InterruptedException {
        latch.await();
    }

}
