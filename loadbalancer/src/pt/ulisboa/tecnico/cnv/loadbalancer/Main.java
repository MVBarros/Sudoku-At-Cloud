package pt.ulisboa.tecnico.cnv.loadbalancer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.cnv.dynamo.DynamoFrontEnd;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.HealthCheckTask;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Main {
    private static final int NUM_THREADS_HEALTH_CHECK = 5;
    private static final int PORT = 9000;

    public static void main(final String[] args) throws Exception {
        DynamoFrontEnd.createTables();

        final HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/instanceAdd", new LBHandler());
        // be aware! infinite pool of threads!
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println(server.getAddress().toString());
    }

    /**
     * Receives request of type url:port?addr=addr
     * Called when a new instance is created: starts health check
     */
    static class LBHandler implements HttpHandler {

        private Executor healthCheckExecutor = Executors.newFixedThreadPool(NUM_THREADS_HEALTH_CHECK); //Should be enough

        @Override
        public void handle(HttpExchange t) throws IOException {
            // Get the query.
            final String query = t.getRequestURI().getQuery();
            System.out.println("> Health Check Query:\t" + query);

            // Break it down into String[].
            final String[] params = query.split("&");
            String addr = null;
            for (final String p : params) {
                String[] splitParam = p.split("=");
                if (splitParam[0].equals("addr")) {
                    addr = splitParam[1];
                    break;
                }
            }
            if (addr != null) {
                //Address was provided, start health check
                try {
                    healthCheckExecutor.execute(new HealthCheckTask(addr));
                    requestSuccess(t);
                } catch (MalformedURLException e) {
                    //Bad URL
                    System.out.println("addr was mal formed: " + addr);
                    requestError(t);
                }
            } else {
                //No address was provided, send error message
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
    }
}
