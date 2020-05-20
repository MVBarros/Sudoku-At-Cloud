package pt.ulisboa.tecnico.cnv.loadbalancer.sudoku;

import pt.ulisboa.tecnico.cnv.loadbalancer.instance.Instance;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.RequestQueue;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.state.InstanceStateSuspected;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.ThreadManager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;


public class SudokuRequest implements Runnable {
    private static final int SUDOKU_REQUEST_SUCCESS = 200;
    private static final double MIN_COST_SCALE = 1.5 * Math.pow(10, -2); //Allow to lose up to ~99% of the cost due to time elapsing
    private static final long REQUEST_COST_LOSS_SLOPE = 10000; //Measured to be less than the average since we prefer to overmeasure instead of undermeasure
    private static final long MAX_REQUEST_COST = (long) (3 * Math.pow(10, 9)); //Maximize to cost to ~ that espected for a 5 minute request, avoiding launching multiple instances for a single gigantic request
    private final SudokuParameters parameters;
    private final long minCost;
    private final Instance instance;
    private long cost;
    private long lastCostCheckTS;

    public SudokuRequest(SudokuParameters parameters, Instance instance) {
        this.parameters = parameters;
        this.cost = parameters.getCost();
        this.minCost = (long) (this.cost * MIN_COST_SCALE);
        this.instance = instance;
        this.lastCostCheckTS = System.currentTimeMillis();
    }

    public SudokuParameters getParameters() {
        return parameters;
    }

    public long getCurrentCost() {
        return Math.min(MAX_REQUEST_COST, currentCost());
    }

    public long estimateCompletionTime() {
        return getCurrentCost() / getCurrentSlope();
    }

    private long currentCost() {
        //Assuming all requests get equal CPU time
        long newCost = Math.max(cost - getCurrentSlope() * getTime(), minCost);
        this.cost = newCost;
        return newCost;
    }

    private long getCurrentSlope() {
        return REQUEST_COST_LOSS_SLOPE / numberOfRequestsInInstance();
    }

    private long numberOfRequestsInInstance() {
        int size = instance.getRequests().size();
        return size == 0 ? 1 : size;
    }

    private long getTime() {
        long currentTs = System.currentTimeMillis();
        long time = currentTs - lastCostCheckTS;
        lastCostCheckTS = currentTs;
        return time;
    }


    public void executeOnNewThread() {
        this.instance.addRequest(this);
        ThreadManager.execute(this);
    }


    public void execute() {
        this.instance.addRequest(this);
        this.run();
    }

    /**
     * Sends Sudoku Request to instance on the other side of @conn
     */
    private void sendRequest(HttpURLConnection conn) {
        System.out.println("Request " + this.parameters + " going to instance " + instance.getId());
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
            }
        } catch (IOException e) {
            instanceError(conn);
        }
    }

    /**
     * Forward sudoku reply from @conn to client that made the request
     */
    private void forwardReply(HttpURLConnection conn) throws IOException {
        this.instance.removeRequest(this);
        this.parameters.forwardReply(getReplyContent(conn));
    }

    private void instanceError(HttpURLConnection conn) {
        instance.removeRequest(this);
        conn.disconnect();
        instance.setState(InstanceStateSuspected.getInstance());
        RequestQueue.addToQueue(this.getParameters());
    }


    private String getReplyContent(HttpURLConnection conn) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }

    @Override
    public void run() {
        HttpURLConnection connection = instance.getSudokuRequestConn(this.getParameters());
        sendRequest(connection);
    }
}
