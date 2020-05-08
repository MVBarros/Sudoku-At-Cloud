package pt.ulisboa.tecnico.cnv.server.task;

import metrics.tools.StatsDLX;
import pt.ulisboa.tecnico.cnv.dynamo.DynamoFrontEnd;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;

public class UploadDLXStatsTask implements Runnable {


    private StatsDLX stats;
    private SolverArgumentParser parser;

    public UploadDLXStatsTask(SolverArgumentParser parser, StatsDLX stats) {
        this.stats = stats;
        this.parser = parser;
    }

    @Override
    public void run() {
        DynamoFrontEnd.uploadStatsDLX(parser, stats);
    }
}
