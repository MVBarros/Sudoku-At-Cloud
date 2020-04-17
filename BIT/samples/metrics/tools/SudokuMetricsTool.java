package metrics.tools;

import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;

public interface SudokuMetricsTool {
    static void saveStats() {};

    static void writeToFile(Stats stats, SolverArgumentParser parser) {};

}

