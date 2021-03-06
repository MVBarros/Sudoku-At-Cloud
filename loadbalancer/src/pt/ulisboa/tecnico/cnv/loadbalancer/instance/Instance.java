package pt.ulisboa.tecnico.cnv.loadbalancer.instance;

import pt.ulisboa.tecnico.cnv.loadbalancer.instance.state.InstanceState;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.state.InstanceStateDead;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.state.InstanceStateHealthy;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.state.InstanceStateSuspected;
import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuParameters;
import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuRequest;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Instance {

    private static final String LB_HANDLER = "/lb";
    private static final String SUDOKU_HANDLER = "/sudoku";
    private static final int HEALTH_CHECK_TIMEOUT = 5000; //5 seconds
    private static final int HEALTH_CHECK_SUCCESS_CODE = 200;
    private static final String HEALTH_CHECK_REQUEST_METHOD = "GET";
    private static final String SUDOKU_REQUEST_METHOD = "GET";
    private static final int UNHEALTHY_THRESHOLD = 1; //1 timeouts before declaring instance as temporary dead
    private static final int DEAD_THRESHOLD = 5; //5 timeouts before declaring instance as permanently dead
    private static final long CHARGE_PERIOD = 60 * 1000 * 60; //1 hour

    private final URL address;
    private final URL LBAddress;
    private final AtomicInteger failureCounter;
    private final Set<SudokuRequest> requests;
    private InstanceState state;
    private final String id;
    private final long startedTS;

    public Instance(String address, String id) throws MalformedURLException {
        this.address = new URL(address);
        this.LBAddress = new URL(address + LB_HANDLER);
        this.requests = Collections.synchronizedSet(new HashSet<SudokuRequest>());
        this.failureCounter = new AtomicInteger(0);
        this.startedTS = System.currentTimeMillis();
        this.id = id;
        setState(InstanceStateSuspected.getInstance()); //Only healthy after passing first health check
    }

    private long getUpTime() {
        return System.currentTimeMillis() - startedTS;
    }

    private long estimateCompletionTime() {
        synchronized (this.requests) {
            long total = 0;
            for (SudokuRequest request : requests) {
                total += request.estimateCompletionTime();
            }
            return  total;
        }
    }

    private String getAddress() {
        return address.toString();
    }

    public long removalCost() {
        //Estimate how much of the time payed will be used once requests complete
        return (getUpTime() + estimateCompletionTime()) % (CHARGE_PERIOD);
    }

    public String getId() {
        return id;
    }

    public void addRequest(SudokuRequest request) {
        System.out.println("Request " + request.getParameters() + " was added to instance " + this.id);
        requests.add(request);
    }

    public void removeRequest(SudokuRequest request) {
        System.out.println("Request " + request.getParameters() + " was removed from instance " + this.id);
        requests.remove(request);
    }

    public void stopRequests() {
        synchronized (requests) {
            for (SudokuRequest request : requests) {
                request.stop();
            }
        }
    }

    public boolean healthCheck() {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) LBAddress.openConnection();
            conn.setReadTimeout(HEALTH_CHECK_TIMEOUT);
            conn.setConnectTimeout(HEALTH_CHECK_TIMEOUT);
            conn.setRequestMethod(HEALTH_CHECK_REQUEST_METHOD);
            conn.connect();
            int status = conn.getResponseCode();
            return status == HEALTH_CHECK_SUCCESS_CODE;
        } catch (Exception e) {
            System.out.println("Health Check for address " + LBAddress + " failed due to exception " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public long getLoad() {
        long load = 0;
        synchronized (requests) {
            for (SudokuRequest request : requests) {
                load += request.getCurrentCost();
            }
        }
        return load;
    }

    public InstanceState getState() {
        return state;
    }

    public void setState(InstanceState state) {
        InstanceState newState = null;
        synchronized (this) {
            if (this.state != InstanceStateDead.getInstance() && state != this.state) {
                //Once dead can never be undead
                this.state = state;
                newState = state;
            }
        }
        if (newState != null) {
            System.out.println("Instance " + id + " is in state " + newState.name());
            newState.newState(this);
        }
    }

    public HttpURLConnection getSudokuRequestConn(SudokuParameters parameters) {
        HttpURLConnection conn;
        try {
            URL sudokuAddress = new URL(sudokuPath(parameters));
            conn = (HttpURLConnection) sudokuAddress.openConnection();
            conn.setRequestMethod(SUDOKU_REQUEST_METHOD);
            return conn;
        } catch (Exception e) {
            return null;
        }
    }

    private String sudokuPath(SudokuParameters parameters) {
        return address.toString() + SUDOKU_HANDLER + "?" + parameters.toString();
    }

    public Set<SudokuRequest> getRequests() {
        return requests;
    }


    public void resetFailureCounter() {
        this.failureCounter.set(0);
        setState(InstanceStateHealthy.getInstance());
    }

    public void incrFailureCounter() {
        int failureCounter = this.failureCounter.incrementAndGet();
        if (failureCounter == DEAD_THRESHOLD) {
            setState(InstanceStateDead.getInstance());
        } else if (failureCounter == UNHEALTHY_THRESHOLD) {
            setState(InstanceStateSuspected.getInstance());
        }

    }

    @Override
    public String toString() {
        return this.getAddress();
    }

    public boolean isHealthy() {
        return this.state == InstanceStateHealthy.getInstance();
    }
}
