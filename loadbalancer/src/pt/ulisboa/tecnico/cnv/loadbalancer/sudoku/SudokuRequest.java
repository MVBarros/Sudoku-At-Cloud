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

    private final SudokuParameters parameters;
    private final long cost;
    private final HttpExchange httpExchange;
    private Instance instance;

    public SudokuRequest(SudokuParameters parameters, HttpExchange httpExchange) {
        this.parameters = parameters;
        this.cost = DynamoFrontEnd.inferCost(parameters);
        System.out.println("Inferred cost for parameters " + parameters + " --> " + this.cost);
        this.httpExchange = httpExchange;
    }

    public SudokuParameters getParameters() {
        return parameters;
    }

    public long getCost() {
        return cost;
    }


    /**
     * Sends Sudoku Request to instance on the other side of @conn
     */
    public void sendRequest(HttpURLConnection conn) {
        try {
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
                instanceError(conn);
                conn.disconnect();
            }
        } catch (IOException e) {
            instanceError(conn);
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

        } catch (IOException e) {
            conn.disconnect();
            System.out.println("Instance finished request but client disconnected: " + instance);
            this.instance.removeRequest(this);
        } finally {
            conn.disconnect();
            System.out.println("Instance finished request: " + instance);
            this.instance.removeRequest(this);
        }
    }

    private void instanceError(HttpURLConnection conn) {
        conn.disconnect();
        this.instance.setState(Instance.InstanceState.UNHEALTHY);
        this.instance.removeRequest(this);
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
        this.instance = instance;
        instance.addRequest(this);
    }
}
