package pt.ulisboa.tecnico.cnv.loadbalancer.instance;

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

    public enum InstanceState {
        HEALTHY,
        UNHEALTHY,
        DEAD;
    }

    private static final String LB_HANDLER = "/lb";
    private static final String SUDOKU_HANDLER = "/sudoku";
    private static final int HEALTH_CHECK_TIMEOUT = 5000; //5 seconds
    private static final int HEALTH_CHECK_SUCCESS_CODE = 200;
    private static final String HEALTH_CHECK_REQUEST_METHOD = "GET";
    private static final String SUDOKU_REQUEST_METHOD = "GET";
    private static final int UNHEALTHY_THRESHOLD = 1; //1 timeouts before declaring instance as temporary dead
    private static final int DEAD_THRESHOLD = 5; //5 timeouts before declaring instance as permanently dead

    private final URL address;
    private final URL LBAddress;
    private final AtomicInteger failureCounter;
    private final Set<SudokuRequest> requests;
    private InstanceState state;

    public Instance(String address) throws MalformedURLException {
        this.address = new URL(address);
        this.LBAddress = new URL(address + LB_HANDLER);
        this.requests = Collections.synchronizedSet(new HashSet<SudokuRequest>());
        this.failureCounter = new AtomicInteger(0);
        this.state = InstanceState.HEALTHY;
    }

    public String getAddress() {
        return address.toString();
    }

    public void addRequest(SudokuRequest request) {
        requests.add(request);
    }

    public void removeRequest(SudokuRequest request) {
        requests.remove(request);
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

    //FIXME Take into account time expiring
    public long getLoad() {
        long load = 0;
        synchronized (requests) {
            for (SudokuRequest request : requests) {
                load += request.getCost();
            }
        }
        return load;
    }

    public InstanceState getState() {
        return state;
    }

    public void setState(InstanceState state) {
        if (state != InstanceState.DEAD && state != this.state) {
            //Once dead can never be undead
            this.state = state;
            if (this.state == InstanceState.HEALTHY) {
                //Wake threads waiting for instance has a new one as come online
                InstanceManager.getInstance().notifyMonitor();
            }
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
        synchronized (failureCounter) {
            this.failureCounter.set(0);
            setState(InstanceState.HEALTHY);
        }
    }


    public void incrFailureCounter() {
        synchronized (failureCounter) {
            int failureCounter = this.failureCounter.incrementAndGet();
            if (failureCounter == DEAD_THRESHOLD) {
                setState(InstanceState.DEAD);
            }
            else if (failureCounter == UNHEALTHY_THRESHOLD) {
                setState(InstanceState.UNHEALTHY);
            }
        }
    }
}
