package pt.ulisboa.tecnico.cnv.solver;

import pt.ulisboa.tecnico.cnv.solver.Solver;

public interface SudokuSolver {
    boolean runSolver(Solver solver);

    void setPuzzle(int[][] puzzle);

    int[][] getPuzzle();
    int[][] getSolution();

    void printPuzzle();
    void printSolution();

    @Override
    String toString();
}
