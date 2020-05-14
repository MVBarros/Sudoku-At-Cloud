package pt.ulisboa.tecnico.cnv.loadbalancer.sudoku;

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
}

