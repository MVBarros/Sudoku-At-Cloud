package pt.ulisboa.tecnico.cnv.loadbalancer.task;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ThreadManager {
    private static Executor globalExecutor = Executors.newCachedThreadPool();
    private ThreadManager() {}

    public static Executor getGlobalExecutor() {
        return globalExecutor;
    }

    public static void execute(Runnable runnable) {
        globalExecutor.execute(runnable);
    }
}
