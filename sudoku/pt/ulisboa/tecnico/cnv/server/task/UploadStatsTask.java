package pt.ulisboa.tecnico.cnv.server.task;

import metrics.tools.RequestStats;
import pt.ulisboa.tecnico.cnv.dynamo.DynamoFrontEnd;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;

public class UploadStatsTask implements Runnable {

    private RequestStats stats;
    private SolverArgumentParser parser;
    private long delta;

    public UploadStatsTask(SolverArgumentParser parser, RequestStats stats, long delta) {
        this.stats = stats;
        this.parser = parser;
        this.delta = delta;
    }

    @Override
    public void run() {
        DynamoFrontEnd.uploadStats(parser, stats, delta);
    }
}
