package pt.ulisboa.tecnico.cnv.loadbalancer.task;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HealthCheckTask implements Runnable {
    private static final int TIMEOUT = 5000; //5 seconds
    private static final int INTERVAL = 10000; //10 seconds
    private static final int UNEALTHY_THRESHOLD = 3; //3 timeouts before declaring instance as dead
    private static final String LB_HANDLER = "/lb";
    private static final String REQUEST_METHOD = "GET";
    private static final String REQUEST_PROTOCOL = "http://";
    private static final int SUCCESS_CODE = 200;

    private final String addr;
    private int failureCounter;
    private URL url;
    private boolean running;
    public HealthCheckTask(String addr) throws MalformedURLException {
        this.addr = REQUEST_PROTOCOL + addr + LB_HANDLER;
        this.failureCounter = 0;
        this.url = new URL(this.addr);
        this.running = true;
    }

    @Override
    public void run() {
        while (true) {
            HttpURLConnection conn = null;
            try {
                if (!running) {
                    return;
                }
                try {
                    Thread.sleep(INTERVAL);
                } catch (InterruptedException e) {
                    System.out.println("Error: Health check thread for address " + addr + " was sleeping and was interrupted");
                }

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(REQUEST_METHOD);
                conn.setReadTimeout(TIMEOUT);
                conn.setConnectTimeout(TIMEOUT);
                conn.connect();
            } catch (IOException e) {
                System.out.println("Could not connect to url " + addr + " got error: " + e.getMessage());
                healthCheckFailure(conn);
                if (conn != null) {
                    conn.disconnect();
                }
                continue;
            }
            try {
                int status = conn.getResponseCode();
                if (status == SUCCESS_CODE) {
                    healthCheckSuccess();
                } else {
                    healthCheckFailure(conn);
                }
            } catch (IOException e) {
                System.out.println("Error sending request to url " + addr + "\nError message: " + e.getMessage());
                healthCheckFailure(conn);
            }

            conn.disconnect();
        }
    }

    private void healthCheckSuccess() {
        failureCounter = 0;
        System.out.println("Success sending health check to addr " + addr);
    }


    private void healthCheckFailure(HttpURLConnection conn) {
        failureCounter++;
        System.out.println("Failure sending health check to addr " + addr + "\nCurrent counter: " + failureCounter);
        if (failureCounter == UNEALTHY_THRESHOLD) {
            System.out.println("Address is dead: " + addr);
            if (conn != null) {
                conn.disconnect();
            }
            running = false;
        }
    }
}
