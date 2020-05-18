package pt.ulisboa.tecnico.cnv.autoscaler.task;

import pt.ulisboa.tecnico.cnv.autoscaler.AutoScaler;

public class CreateInstanceTask implements Runnable {

    @Override
    public void run() {
        AutoScaler.createInstance();
    }
}
