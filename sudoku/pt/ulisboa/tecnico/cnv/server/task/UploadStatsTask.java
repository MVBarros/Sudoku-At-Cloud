package pt.ulisboa.tecnico.cnv.server.task;

import metrics.tools.Stats;
import pt.ulisboa.tecnico.cnv.dynamo.DynamoFrontEnd;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;

public class UploadStatsTask implements Runnable {

    private Stats stats;
    private SolverArgumentParser parser;
    private long deltaT;
    public UploadStatsTask(SolverArgumentParser parser, Stats stats, long deltaT) {
        this.stats = stats;
        this.parser = parser;
        this.deltaT = deltaT;
    }

    @Override
    public void run() {
        DynamoFrontEnd.uploadStats(parser, stats, deltaT);
    }
}
