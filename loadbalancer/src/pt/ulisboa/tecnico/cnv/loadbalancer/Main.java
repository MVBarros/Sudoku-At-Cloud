package pt.ulisboa.tecnico.cnv.loadbalancer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.cnv.dynamo.DynamoFrontEnd;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.InstanceManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.util.concurrent.Executors;

public class Main {
    private static final int PORT = 9000;

    public static void main(final String[] args) throws Exception {
        DynamoFrontEnd.createTables();

        final HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/newInstance", new LBHandler());
        // be aware! infinite pool of threads!
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Load Balancer running at address " + server.getAddress().toString());
    }

    /**
     * Receives request of type url:port?addr=addr
     * Called when a new instance is created: starts health check
     */
    static class LBHandler implements HttpHandler {


        @Override
        public void handle(HttpExchange t) throws IOException {
            final String query = t.getRequestURI().getQuery();
            System.out.println("> Health Check Query:\t" + query);

            String address = getAddress(query);
            if (address != null) {
                addInstance(t, address);
            } else {
                System.out.println("No address provided");
                requestError(t);
            }
        }

        private void addInstance(HttpExchange t, String address) throws IOException {
            try {
                InstanceManager.getInstance().addInstance(address);
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
                if (splitParam[0].equals("addr")) {
                    address = splitParam[1];
                    break;
                }
            }
            return address;
        }
    }
}
