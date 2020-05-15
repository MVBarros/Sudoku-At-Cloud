package pt.ulisboa.tecnico.cnv.loadbalancer.sudoku;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import pt.ulisboa.tecnico.cnv.dynamo.DynamoFrontEnd;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.Instance;

import java.io.*;
import java.net.HttpURLConnection;


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
                //When the health check fails the request will be sent to another machine so can just return
                conn.disconnect();
            }
        } catch (IOException e) {
            //Try and send to a new instance immediately
            conn.disconnect();
        }
    }

    /**
     * Forward sudoku reply from @conn to client that made the request
     */
    public void forwardReply(HttpURLConnection conn) throws IOException {
        try {
            String reply = getReply(conn);

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
            OutputStreamWriter out = new OutputStreamWriter(os, "UTF-8");
            out.write(reply);
            out.flush();
            out.close();
            os.close();

            System.out.println("Sent sudoku response to " + this.httpExchange.getRemoteAddress().toString());

        } finally {
            conn.disconnect();
            System.out.println("Instance finished request: " + instance);
            this.instance.removeRequest(this);
        }
    }

    private String getReply(HttpURLConnection conn) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
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
