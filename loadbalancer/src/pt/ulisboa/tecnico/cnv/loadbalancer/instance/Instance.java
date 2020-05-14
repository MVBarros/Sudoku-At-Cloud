package pt.ulisboa.tecnico.cnv.loadbalancer.instance;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Instance {
    private static final String LB_HANDLER = "/lb";
    private static final int HEALTH_CHECK_TIMEOUT = 5000; //5 seconds
    private static final int HEALTH_CHECK_SUCCESS_CODE = 200;
    private static final String HEALTH_CHECK_REQUEST_METHOD = "GET";

    private final URL address;
    private final URL LBAddress;

    public Instance(String address) throws MalformedURLException {
        this.address = new URL(address);
        this.LBAddress = new URL(address + LB_HANDLER);
    }

    public String getAddress() {
        return address.toString();
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
}
