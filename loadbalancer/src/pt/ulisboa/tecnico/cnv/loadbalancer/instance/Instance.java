package pt.ulisboa.tecnico.cnv.loadbalancer.instance;

import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuParameters;
import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuRequest;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Instance {
    private static final String LB_HANDLER = "/lb";
    private static final String SUDOKU_HANDLER = "/sudoku";
    private static final int HEALTH_CHECK_TIMEOUT = 5000; //5 seconds
    private static final int HEALTH_CHECK_SUCCESS_CODE = 200;
    private static final String HEALTH_CHECK_REQUEST_METHOD = "GET";

    private static final String SUDOKU_REQUEST_METHOD = "GET";


    private final URL address;
    private final URL LBAddress;
    private final URL sudokuAddress;

    private final Set<SudokuRequest> requests;

    public Instance(String address) throws MalformedURLException {
        this.address = new URL(address);
        this.LBAddress = new URL(address + LB_HANDLER);
        this.sudokuAddress = new URL(address + SUDOKU_HANDLER);
        this.requests = Collections.synchronizedSet(new HashSet<SudokuRequest>());
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

    public long getLoad() {
        long load = 0;
        synchronized (requests) {
            for (SudokuRequest request : requests) {
                load += request.getCost();
            }
        }
        return load;
    }

    public HttpURLConnection getSudokuRequestConn(SudokuParameters parameters) {
        HttpURLConnection conn;
        try {
            URL sudokuAddress = new URL(sudokuPath(parameters));
            conn = (HttpURLConnection) sudokuAddress.openConnection();
            conn.connect();
            conn.setRequestMethod(SUDOKU_REQUEST_METHOD);
            return conn;
        } catch (Exception e) {
            return null;
        }
    }

    private String sudokuPath(SudokuParameters parameters) {
        return address.toString() + SUDOKU_HANDLER + "?" + parameters.toString();
    }
}
