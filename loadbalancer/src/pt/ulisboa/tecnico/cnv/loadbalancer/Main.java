package pt.ulisboa.tecnico.cnv.loadbalancer;

import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.cnv.dynamo.DynamoFrontEnd;
import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuParameters;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    private static final int PORT = 9000;

    public static void main(final String[] args) throws Exception {
        DynamoFrontEnd.createTables();

        final HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/newInstance", new NewInstanceHandler());
        server.createContext("/sudoku", new SudokuHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("Load balancer running at address " + server.getAddress().toString());
    }


}
