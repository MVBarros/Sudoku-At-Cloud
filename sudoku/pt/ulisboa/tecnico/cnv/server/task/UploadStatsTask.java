package pt.ulisboa.tecnico.cnv.server.task;

import metrics.tools.Stats;
import metrics.tools.StatsBFS;
import pt.ulisboa.tecnico.cnv.dynamo.DynamoFrontEnd;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;

public class UploadStatsTask implements Runnable {

    private Stats stats;
    private SolverArgumentParser parser;

    public UploadStatsTask(SolverArgumentParser parser, Stats stats) {
        this.stats = stats;
        this.parser = parser;
    }

    @Override
    public void run() {
        DynamoFrontEnd.uploadStats(parser, stats);
    }
}
