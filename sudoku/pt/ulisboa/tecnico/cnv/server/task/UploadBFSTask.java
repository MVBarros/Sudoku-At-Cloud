package pt.ulisboa.tecnico.cnv.server.task;

import metrics.tools.StatsBFS;
import pt.ulisboa.tecnico.cnv.dynamo.DynamoFrontEnd;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;

public class UploadBFSTask implements Runnable {

    private StatsBFS stats;
    private SolverArgumentParser parser;

    public UploadBFSTask(SolverArgumentParser parser, StatsBFS stats) {
        this.stats = stats;
        this.parser = parser;
    }

    @Override
    public void run() {
        DynamoFrontEnd.uploadStatsBFS(parser, stats);
    }
}
