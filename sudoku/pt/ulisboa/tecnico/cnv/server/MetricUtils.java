package pt.ulisboa.tecnico.cnv.server;

import org.json.JSONObject;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;
import org.json.JSONException;
import java.util.Collections;
import java.util.Arrays;

public class MetricUtils {

    public static int getBoardZeros(SolverArgumentParser parser) {
        return Collections.frequency(Arrays.asList(parser.getPuzzleBoard().replace("[", "").replace("]", "").split(",")), "0");
    }

    public static JSONObject toJSON(SolverArgumentParser parser) {
        JSONObject obj = new JSONObject();
        obj.put("N1", parser.getN1());
        obj.put("N2", parser.getN2());
        obj.put("UN", parser.getUn());
        obj.put("Input Board", parser.getInputBoard());
        obj.put("Puzzle Board", parser.getPuzzleBoard());
        obj.put("Board Zeros", getBoardZeros(parser));
        obj.put("Solver Strategy", parser.getSolverStrategy().toString());
        return obj;
    }
}