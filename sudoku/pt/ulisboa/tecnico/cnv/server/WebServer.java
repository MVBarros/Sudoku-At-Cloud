package pt.ulisboa.tecnico.cnv.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import metrics.tools.RequestStats;
import metrics.tools.SudokuMetricsTool;
import org.json.JSONArray;
import pt.ulisboa.tecnico.cnv.server.task.UploadStatsTask;
import pt.ulisboa.tecnico.cnv.solver.Solver;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;
import pt.ulisboa.tecnico.cnv.solver.SolverFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WebServer {

    /**
     * One thread should be enough to upload request metrics
     */
    private static Executor metricsUploadExecutor = Executors.newSingleThreadExecutor();

    public static void main(final String[] args) throws Exception {

        final HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);


        server.createContext("/sudoku", new MyHandler());
        server.createContext("/lb", new LBHandler());

        // be aware! infinite pool of threads!
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println(server.getAddress().toString());
    }

    private static String parseRequestBody(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader(is, "utf-8");
        BufferedReader br = new BufferedReader(isr);

        // From now on, the right way of moving from bytes to utf-8 characters:

        int b;
        StringBuilder buf = new StringBuilder(512);
        while ((b = br.read()) != -1) {
            buf.append((char) b);

        }

        br.close();
        isr.close();

        return buf.toString();
    }

    private static void saveStats(SolverArgumentParser parser) {
        RequestStats stats = SudokuMetricsTool.getCurrentStats();
        metricsUploadExecutor.execute(new UploadStatsTask(parser, stats));

    }

    static class LBHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.sendResponseHeaders(200, 0);
            OutputStream os = t.getResponseBody();
            os.write(new byte[0]);
            os.close();
        }
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(final HttpExchange t) throws IOException {
            try {
                // Get the query.
                final String query = t.getRequestURI().getQuery();
                System.out.println("> Query:\t" + query);

                // Break it down into String[].
                final String[] params = query.split("&");

                // Store as if it was a direct call to SolverMain.
                final ArrayList<String> newArgs = new ArrayList<>();
                for (final String p : params) {
                    final String[] splitParam = p.split("=");
                    newArgs.add("-" + splitParam[0]);
                    newArgs.add(splitParam[1]);
                }
                newArgs.add("-b");
                newArgs.add(parseRequestBody(t.getRequestBody()));

                newArgs.add("-d");

                // Store from ArrayList into regular String[].
                final String[] args = new String[newArgs.size()];
                int i = 0;
                for (String arg : newArgs) {
                    args[i] = arg;
                    i++;
                }
                // Get user-provided flags.
                 final SolverArgumentParser ap = new SolverArgumentParser(args);


                // Create solver instance from factory.
                final Solver s = SolverFactory.getInstance().makeSolver(ap);

                //Solve sudoku puzzle
                JSONArray solution = s.solveSudoku();
                saveStats(ap);

                // Send response to browser.
                final Headers hdrs = t.getResponseHeaders();

                //t.sendResponseHeaders(200, responseFile.length());


                ///hdrs.add("Content-Type", "image/png");
                hdrs.add("Content-Type", "application/json");

                hdrs.add("Access-Control-Allow-Origin", "*");

                hdrs.add("Access-Control-Allow-Credentials", "true");
                hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
                hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

                t.sendResponseHeaders(200, solution.toString().length());


                final OutputStream os = t.getResponseBody();
                OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                osw.write(solution.toString());
                osw.flush();
                osw.close();

                os.close();

                System.out.println("> Sent response to " + t.getRemoteAddress().toString());
            } finally {
                SudokuMetricsTool.removeCurrentStats();
            }
        }
    }
}
