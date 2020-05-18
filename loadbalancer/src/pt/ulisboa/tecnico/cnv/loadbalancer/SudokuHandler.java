package pt.ulisboa.tecnico.cnv.loadbalancer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.Instance;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.InstanceManager;
import pt.ulisboa.tecnico.cnv.loadbalancer.instance.RequestQueue;
import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuParameters;
import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuParametersBuilder;
import pt.ulisboa.tecnico.cnv.loadbalancer.sudoku.SudokuRequest;
import pt.ulisboa.tecnico.cnv.loadbalancer.task.ThreadManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SudokuHandler implements HttpHandler {
    private static final String N1_KEY = "n1";
    private static final String N2_KEY = "n2";
    private static final String UN_KEY = "un";
    private static final String INPUT_BOARD_KEY = "i";
    private static final String STRATEGY_KEY = "s";

    @Override
    public void handle(HttpExchange t) throws IOException {
        final String query = t.getRequestURI().getQuery();
        System.out.println("> Sudoku Query:\t" + query);
        String body = parseRequestBody(t.getRequestBody());

        SudokuParameters parameters = parseRequest(query, body, t);
        //Try and get instance, if failed add to queue and it will eventually be handled
        Instance instance = InstanceManager.getBestInstance();
        if (instance == null) {
            RequestQueue.addToQueue(parameters);
        } else {
            //ThreadManager.execute(new SudokuRequest(parameters, instance));
            //No need to start new thread here
            new SudokuRequest(parameters, instance).run();
        }
    }


    private SudokuParameters parseRequest(String query, String body, HttpExchange exchange) {
        SudokuParametersBuilder builder = SudokuParameters.newBuilder();
        String[] parameters = query.split("&");

        for (String parameter : parameters) {
            String[] splitParam = parameter.split("=");
            String key = splitParam[0];
            String value = splitParam[1];
            switch (key) {
                case N1_KEY:
                    builder.setN1(Integer.parseInt(value));
                    break;
                case N2_KEY:
                    builder.setN2(Integer.parseInt(value));
                    break;
                case UN_KEY:
                    builder.setUn(Integer.parseInt(value));
                    break;
                case INPUT_BOARD_KEY:
                    builder.setInputBoard(value);
                    break;
                case STRATEGY_KEY:
                    builder.setStrategy(value);
                    break;
                default:
                    System.out.println(String.format("Sudoku Handler: Received unknown parameter: (%s,%s)", key, value));
                    break;
            }
        }
        return builder.setPuzzleBoard(body)
                .setExchange(exchange)
                .build();
    }

    /**
     * Parse Sudoku Board
     */
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

}
