package pt.ulisboa.tecnico.cnv.loadbalancer.sudoku;

import metrics.tools.StatsBFS;
import metrics.tools.StatsCP;
import metrics.tools.StatsDLX;

public class SudokuParameters {
    enum Strategy {
        BFS("BFS"),
        CP("CP"),
        DLX("DLX");

        private final String strategy;

        Strategy(String strategy) {
            this.strategy = strategy;
        }

        public String getStrategy() {
            return strategy;
        }
    }

    private final int n1;
    private final int n2;
    private final int un;
    private final String inputBoard;
    private final String puzzleBoard;
    private final Strategy strategy;

    SudokuParameters(int n1, int n2, int un, String inputBoard, String puzzleBoard, Strategy strategy) {
        this.n1 = n1;
        this.n2 = n2;
        this.inputBoard = inputBoard;
        this.puzzleBoard = puzzleBoard;
        this.un = un;
        this.strategy = strategy;
    }

    public int getN1() {
        return n1;
    }

    public int getN2() {
        return n2;
    }

    public String getInputBoard() {
        return inputBoard;
    }

    public String getPuzzleBoard() {
        return puzzleBoard;
    }

    public int getUn() {
        return un;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public static SudokuParametersBuilder newBuilder() {
        return new SudokuParametersBuilder();
    }

    @Override
    public String toString() {
        return String.format("s=%s&un=%d&n1=%d&n2=%d&i=%s", getStrategy().name(), getUn(), getN1(), getN2(), getInputBoard());
    }

    public String getTableName() {
        switch (strategy) {
            case BFS:
                return StatsBFS.BFS_TABLE_NAME;
            case CP:
                return StatsCP.CP_TABLE_NAME;
            case DLX:
                return StatsDLX.DLX_TABLE_NAME;
        }
        //Should never reach here, but just in case assume BFS
        return StatsBFS.BFS_TABLE_NAME;
    }
}

