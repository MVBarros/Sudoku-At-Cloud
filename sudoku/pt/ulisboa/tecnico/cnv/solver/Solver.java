package pt.ulisboa.tecnico.cnv.solver;

import org.json.JSONArray;

public class Solver {


    private final SolverArgumentParser ap;
    private final SudokuSolver strategy;
    private int[][] board = null;


    protected Solver(final SolverArgumentParser ap, SudokuSolver strategy) {
        this.ap = ap;
        this.strategy = strategy;
        this.board = new int[ap.getN1()][ap.getN2()];
        JSONArray jsonArray = new JSONArray(ap.getPuzzleBoard());
        int N = jsonArray.length();

        for(int i = 0; i<N; i++) {
            JSONArray line = jsonArray.getJSONArray(i);
            for (int j = 0; j<N; j++) {
                board[i][j] = line.getInt(j);
            }
        }

    }

    public boolean isDebugging() {
        return this.ap.isDebugging();
    }

    public int[][] getBoard() {
        return board;
    }

    public Integer getN1() {
        return this.ap.getN1();
    }

    public Integer getN2() {
        return this.ap.getN2();
    }

    @Override
    public String toString() {
        return ap.getPuzzleBoard();
    }

    public JSONArray solveSudoku() {
        strategy.setPuzzle(getBoard());


        if(ap.isDebugging()) {
            System.out.println(String.format("> Solving %dx%d sudoku puzzle", getN1(), getN2()));
            System.out.println(String.format("> Strategy: %s", ap.getSolverStrategy().toString()));
            strategy.printPuzzle();
        }


        //final long startTime = System.nanoTime();
        strategy.runSolver(this);
        //final long elapsedTime = System.nanoTime() - startTime;

        if(ap.isDebugging()) {
            strategy.printSolution();
            //System.out.println(" Solution found in " + (elapsedTime*1e-6) + " ms");
        }

        int[][] solution = strategy.getSolution();

        JSONArray jsonArray = new JSONArray();
        for(int lin = 0; lin<ap.getN1(); lin++){
            JSONArray line = new JSONArray();
            for(int col = 0; col<ap.getN2(); col++){
                line.put(solution[lin][col]);
            }
            jsonArray.put(line);
        }
        return jsonArray;
    }


    /*
     * Running to the next map position.
     */

    public void run() {
        final Integer velocity = 125;
        final int numberOfTimeMeasurements = 10500;
        int minVelocityLoops = 3500;

        long currentRunTime = 0;

        // Time to run into the next maze position.
        for(int k = 0; k < minVelocityLoops / velocity; k++) {
            for(int i = 0; i < numberOfTimeMeasurements; i++) {
                currentRunTime = System.currentTimeMillis();
            }
        }
    }

}
