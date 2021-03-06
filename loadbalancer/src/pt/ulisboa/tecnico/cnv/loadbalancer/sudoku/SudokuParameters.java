package pt.ulisboa.tecnico.cnv.loadbalancer.sudoku;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import pt.ulisboa.tecnico.cnv.dynamo.DynamoFrontEnd;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class SudokuParameters {
    public enum Strategy {
        BFS,
        CP,
        DLX;
    }

    private final int n1;
    private final int n2;
    private final int un;
    private final long cost;
    private final String inputBoard;
    private final String puzzleBoard;
    private final Strategy strategy;
    private final HttpExchange exchange;
    private boolean answered;

    SudokuParameters(int n1, int n2, int un, String inputBoard, String puzzleBoard, Strategy strategy, HttpExchange exchange) {
        this.n1 = n1;
        this.n2 = n2;
        this.inputBoard = inputBoard;
        this.puzzleBoard = puzzleBoard;
        this.un = un;
        this.strategy = strategy;
        this.exchange = exchange;
        this.answered = false;
        this.cost = DynamoFrontEnd.inferCost(this);
    }

    public int getN1() {
        return n1;
    }

    public int getN2() {
        return n2;
    }

    public String getInputBoard() {
        return inputBoard;
    }

    public String getPuzzleBoard() {
        return puzzleBoard;
    }

    public int getUn() {
        return un;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public static SudokuParametersBuilder newBuilder() {
        return new SudokuParametersBuilder();
    }

    public long getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return String.format("s=%s&un=%d&n1=%d&n2=%d&i=%s", getStrategy().name(), getUn(), getN1(), getN2(), getInputBoard());
    }

    public String getTableName() {
        switch (strategy) {
            case BFS:
                return DynamoFrontEnd.BFS_TABLE_NAME;
            case CP:
                return DynamoFrontEnd.CP_TABLE_NAME;
            case DLX:
                return DynamoFrontEnd.DLX_TABLE_NAME;
        }
        //Should never reach here, but just in case assume BFS
        return DynamoFrontEnd.BFS_TABLE_NAME;
    }

    public HttpExchange getExchange() {
        return exchange;
    }

    /**
     * Forward sudoku reply from @conn to client that made the request
     */
    public void forwardReply(String reply) {
        synchronized (this) {
            if (!this.answered) {
                try {
                    sendHeaders(reply);
                    sendBody(reply);
                    System.out.println("Request finished: " + this.toString());
                } catch (IOException e) {
                    System.out.println("client disconnected for request " + this.toString());
                } finally {
                    this.answered = true;
                }
            }
        }
    }

    private void sendHeaders(String reply) throws IOException {
        //Send headers
        final Headers headers = exchange.getResponseHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Credentials", "true");
        headers.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
        exchange.sendResponseHeaders(200, reply.length());
    }

    private void sendBody(String reply) throws IOException {
        final OutputStream os = exchange.getResponseBody();
        OutputStreamWriter out = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        out.write(reply);
        out.flush();
        out.close();
        os.close();
    }
}

