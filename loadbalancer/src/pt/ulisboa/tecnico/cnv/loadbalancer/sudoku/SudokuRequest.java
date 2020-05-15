package pt.ulisboa.tecnico.cnv.loadbalancer.sudoku;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import pt.ulisboa.tecnico.cnv.dynamo.DynamoFrontEnd;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.Instance;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.InstanceManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;


public class SudokuRequest {
    private static final int SUDOKU_REQUEST_SUCCESS = 200;
    private static final long COST_LOSS_PER_MILLISECOND = 0; //TODO
    private static final double MIN_COST_SCALE = 0.1;

    private final SudokuParameters parameters;
    private final long cost;
    private final HttpExchange httpExchange;
    private final Instance instance;
    private long sentTime = System.currentTimeMillis();
    private boolean finished;

    public SudokuRequest(SudokuParameters parameters, Instance instance) {
        this.parameters = parameters;
        this.cost = DynamoFrontEnd.inferCost(parameters);
        System.out.println("Inferred cost for parameters " + parameters + " --> " + this.cost);
        this.httpExchange = parameters.getExchange();
        this.instance = instance;
        this.finished = false;
    }

    public SudokuParameters getParameters() {
        return parameters;
    }

    public long getCost() {
        return Math.min( (long) (cost * MIN_COST_SCALE), cost - COST_LOSS_PER_MILLISECOND * (System.currentTimeMillis() - sentTime));
    }


    /**
     * Sends Sudoku Request to instance on the other side of @conn
     */
    public void sendRequest(HttpURLConnection conn) {
        this.instance.addRequest(this);
        try {
            sentTime = System.currentTimeMillis();
            conn.setRequestProperty("Content-Type", "application/json");

            conn.setDoOutput(true);

            //Write body
            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            out.writeBytes(this.parameters.getPuzzleBoard());
            out.flush();
            out.close();

            int status = conn.getResponseCode();
            if (status == SUDOKU_REQUEST_SUCCESS) {
                forwardReply(conn);
            } else {
                instanceError(conn, instance);
                conn.disconnect();
            }
        } catch (IOException e) {
            instanceError(conn, instance);
            conn.disconnect();

        }
    }

    /**
     * Forward sudoku reply from @conn to client that made the request
     */
    private void forwardReply(HttpURLConnection conn) throws IOException {

        String reply = getReply(conn);
        try {
            //Send headers
            final Headers headers = this.httpExchange.getResponseHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Credentials", "true");
            headers.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
            this.httpExchange.sendResponseHeaders(200, reply.length());

            //Send content
            final OutputStream os = httpExchange.getResponseBody();
            OutputStreamWriter out = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            out.write(reply);
            out.flush();
            out.close();
            os.close();

            System.out.println("Sent sudoku response to " + this.httpExchange.getRemoteAddress().toString());
            System.out.println("Instance finished request: " + instance);
        } catch (IOException e) {
            System.out.println("client disconnected");
        } finally {
            conn.disconnect();
            this.instance.removeRequest(this);
            this.finished = true;
        }
    }

    private void instanceError(HttpURLConnection conn, Instance instance) {
        conn.disconnect();
        instance.setState(Instance.InstanceState.UNHEALTHY);
        instance.removeRequest(this);
        InstanceManager.getInstance().sendRequest(this);
    }

    private String getReply(HttpURLConnection conn) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }

    public void setInstance(Instance instance) {
        instance.addRequest(this);
    }

    public boolean isFinished() {
        return finished;
    }

    public HttpExchange getHttpExchange() {
        return httpExchange;
    }
}
