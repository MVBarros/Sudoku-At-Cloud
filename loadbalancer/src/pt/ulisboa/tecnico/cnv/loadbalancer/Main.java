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

        for (int instance = 0; instance < ScalingTask.MIN_NUMBER_INSTANCES; instance++) {
            ThreadManager.execute(new CreateInstanceTask());
        }

        final HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/sudoku", new SudokuHandler());
        server.setExecutor(ThreadManager.getGlobalExecutor());
        server.start();

        System.out.println("Load balancer running at address " + server.getAddress().toString());
    }
}
