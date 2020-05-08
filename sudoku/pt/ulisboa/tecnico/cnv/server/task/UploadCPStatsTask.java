package pt.ulisboa.tecnico.cnv.server.task;

import metrics.tools.StatsCP;
import pt.ulisboa.tecnico.cnv.dynamo.DynamoFrontEnd;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;

public class UploadCPStatsTask implements Runnable {

    private StatsCP stats;
    private SolverArgumentParser parser;

    public UploadCPStatsTask(SolverArgumentParser parser, StatsCP stats) {
        this.stats = stats;
        this.parser = parser;
    }

    @Override
    public void run() {
        DynamoFrontEnd.uploadStatsCP(parser, stats);

    }
}
