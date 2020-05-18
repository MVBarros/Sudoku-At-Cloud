package pt.ulisboa.tecnico.cnv.loadbalancer;

import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.cnv.autoscaler.task.CreateInstanceTask;
import pt.ulisboa.tecnico.cnv.autoscaler.task.ScalingTask;
import pt.ulisboa.tecnico.cnv.dynamo.DynamoFrontEnd;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.ThreadManager;

import java.net.InetSocketAddress;

public class Main {
    private static final int PORT = 9000;

    public static void main(final String[] args) throws Exception {
        DynamoFrontEnd.createTables();
        CreateInstanceTask[] tasks = new CreateInstanceTask[ScalingTask.MIN_NUMBER_INSTANCES];
        for (int i = 0; i < ScalingTask.MIN_NUMBER_INSTANCES; i++) {
            tasks[i] = new CreateInstanceTask();
        }

        System.out.println("Creating " +  ScalingTask.MIN_NUMBER_INSTANCES + " instances");
        for (int i = 0; i < ScalingTask.MIN_NUMBER_INSTANCES; i++) {
            ThreadManager.execute(tasks[i]);
        }

        for (int i = 0; i < ScalingTask.MIN_NUMBER_INSTANCES; i++) {
            tasks[i].waitFinish();
        }

        //Start scaling measurements
        ThreadManager.execute(new ScalingTask());

        System.out.println("Created " +  ScalingTask.MIN_NUMBER_INSTANCES + " instances successfully");

        final HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/sudoku", new SudokuHandler());
        server.setExecutor(ThreadManager.getGlobalExecutor());
        server.start();

        System.out.println("Load balancer running at address " + server.getAddress().toString());
    }
}
