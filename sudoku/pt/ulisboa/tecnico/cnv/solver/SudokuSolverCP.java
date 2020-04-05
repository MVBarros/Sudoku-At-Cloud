package pt.ulisboa.tecnico.cnv.solver;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mikaelbrevik
 */
public class SudokuSolverCP extends AbstractSudokuSolver{


    public SudokuSolverCP() {
        super();
        name = "Constraint Programming Solver";

    }

    public SudokuSolverCP(int[][] puzzle) {
        super(puzzle);
        name = "Constraint Programming Solver";
    }

    /**
     * Check if input follows given constraints:
     * 
     * 1. Distinct values in row.
     * 2. Distinct values in column.
     * 3. Distinct values in 3x3 square.
     * 
     * @param pos Position of input
     * @param value Value of input
     * @return boolean Boolean value of if the input follows constraints.
     */
    private boolean followsConstraints(Point pos, int value) {

        // Constraint 1: Distinct in row
        for (int i = 0; i < SIZE; i++) {
            if (solution[i][pos.y] == value) {
                return false;
            }
        }

        // Constraint 2: Distinct in column
        for (int i = 0; i < SIZE; i++) {
            if (solution[pos.x][i] == value) {
                return false;
            }
        }

        // Constraint 3: Distinct in square
        for (int i = 0; i < BOX_SIZE; i++) {
            for (int j = 0; j < BOX_SIZE; j++) {
                if (solution[(pos.x / BOX_SIZE) * BOX_SIZE + i][(pos.y / BOX_SIZE) * BOX_SIZE + j] == value) {
                    return false;
                }
            }
        }

        // Follows all constraints. Return true. 
        return true;
    }



    /**
     * Public accessible method for solving sudoku board. 
     * A wrapper for the recursive function. 
     * 
     * 
     * @return boolean
     */
    public boolean runSolver(Solver solver) {
        return runSolver(0, solver);
    }

    /**
     * Recursive method for inserting values to sudoku board.
     * Uses test and generate paradigm from CLP. 
     * 
     * 
     * @param i - Should be initialized with 0. Used by recursion
     * @return boolean - If value is inserted. 
     */
    private boolean runSolver(int i, Solver solver) {
        
        if (i >= (SIZE*SIZE)) {
            return true;
        }


        int y = i / SIZE; // Find y value
        int x = i - y*SIZE; // find x value
        
        // System.out.println("("+x+","+y+")");
        
        // Check if pre-filled
        if(solution[x][y] != 0) {
            return runSolver(i+1, solver);
        }else{
            //solver.run();
        }
        
        for (int value = 1; value <= SIZE; ++value) {
            solver.run();
            if (!followsConstraints(new Point(x, y), value)) {
                continue;
            }
            
            solution[x][y] = value;
            
            if (runSolver(i+1, solver)) {
                return true;
            }
        }
        
        // Could not insert value. Reset value.        
        solution[x][y] = 0;
        return false;
    }

    public boolean checkSudoku() {

        // Check for valid board. 
        for (int i = 0; i < SIZE; i++) {
            List<Integer> exsistsRow = new ArrayList<Integer>();
            List<Integer> exsistsCol = new ArrayList<Integer>();
            List<Integer> exsistsSquare = new ArrayList<Integer>();

            for (int j = 0; j < SIZE; j++) {

                if (solution[i][j] == 0 || solution[j][i] == 0) {
                    return false;
                }
                if (exsistsRow.contains(solution[i][j])) {
                    return false;
                }


                if (exsistsCol.contains(solution[j][i])) {
                    return false;
                }

                exsistsRow.add(solution[i][j]);
                exsistsCol.add(solution[j][i]);

                if (j < BOX_SIZE) {
                    for (int k = 0; k < BOX_SIZE; k++) {
                        int val = solution[j + BOX_SIZE * (i % BOX_SIZE)][k + BOX_SIZE * (i / BOX_SIZE)];
                        if (exsistsSquare.contains(val)) {
                            return false;
                        }
                        exsistsSquare.add(val);
                    }
                }
            }

        }

        return true;
    }
    public static List<Integer> primeNumbersBruteForce(int n) {
        List<Integer> collect = new ArrayList<>();
        for (int i = 2; i < n; i++) {
            if (isPrime(i)) {
                collect.add(i);
            }
        }

        return collect;


    }
    private static boolean isPrime(int number) {

        if (number <= 1) return false;    //  1 is not prime and also not composite

        for (int i = 2; i * i <= number; i++) {
            if (number % i == 0) {
                return false;
            }
        }
        return true;
    }
}
