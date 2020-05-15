package pt.ulisboa.tecnico.cnv.loadbalancer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.InstanceManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;

/**
 * Receive request of type addr=address and adds an instance of that address
 */
public class NewInstanceHandler implements HttpHandler {

    private static final String ADDRESS_PARAMETER = "addr";
    private static final String ID_PARAMETER = "id";

    @Override
    public void handle(HttpExchange t) throws IOException {
        final String query = t.getRequestURI().getQuery();
        System.out.println("> Health Check Query:\t" + query);

        String address = getAddress(query);
        String id = getId(query);
        if (address != null && id != null) {
            addInstance(t, address, id);
        } else {
            System.out.println("No address or id provided");
            requestError(t);
        }
    }

    private void addInstance(HttpExchange t, String address, String id) throws IOException {
        try {
            InstanceManager.getInstance().addInstance(address, id);
            requestSuccess(t);
        } catch (MalformedURLException e) {
            System.out.println("Address provided was malformed: " + address);
            requestError(t);
        }
    }

    private void requestError(HttpExchange t) throws IOException {
        t.sendResponseHeaders(400, 0);
        OutputStream os = t.getResponseBody();
        os.write(new byte[0]);
        os.close();
    }

    private void requestSuccess(HttpExchange t) throws IOException {
        t.sendResponseHeaders(200, 0);
        OutputStream os = t.getResponseBody();
        os.write(new byte[0]);
        os.close();
    }


    private String getAddress(String query) {
        final String[] params = query.split("&");
        String address = null;
        for (final String p : params) {
            String[] splitParam = p.split("=");
            if (splitParam[0].equals(ADDRESS_PARAMETER)) {
                address = splitParam[1];
                break;
            }
        }
        return address;
    }


    private String getId(String query) {
        final String[] params = query.split("&");
        String id = null;
        for (final String p : params) {
            String[] splitParam = p.split("=");
            if (splitParam[0].equals(ID_PARAMETER)) {
                id = splitParam[1];
                break;
            }
        }
        return id;
    }
}
