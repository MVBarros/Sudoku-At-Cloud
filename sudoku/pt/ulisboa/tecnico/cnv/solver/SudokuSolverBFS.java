package pt.ulisboa.tecnico.cnv.solver;

import java.util.ArrayList;
import java.util.List;

public class SudokuSolverBFS extends  AbstractSudokuSolver {

    public SudokuSolverBFS() {
        super();
        name = "Brute-Force Solver";
    }

    // Set up game solution format
    public SudokuSolverBFS(int[][] puzzle){
       super(puzzle);
        name = "Brute-Force Solver";
    }

    // Check for possible numbers in rows, columns, and smaller 3x3 grid
    private boolean rowCheck(int row, int num){
        for(int i = 0; i < SIZE; i++){
            if(solution[row][i] == num){
                return true;
            }
        }
        // default false means there is no constraint in the cell
        return false;
    }

    private boolean colCheck(int col, int num){
        for(int i = 0; i < SIZE; i++){
            if(solution[i][col] == num){
                return true;
            }
        }
        return false;
    }

    private boolean boxCheck(int row, int col, int num){
        int rowStart = row - row % BOX_SIZE;
        int colStart = col - col% BOX_SIZE;
        for(int i = rowStart; i < rowStart + BOX_SIZE; i++){
            for(int j = colStart; j < colStart + BOX_SIZE; j++){
                if(solution[i][j] == num){
                    return true;
                }
            }
        }
        return false;
    }

    // Method to check that there are no constraints in the row, column, or 3x3 grid
    // Runs only when the check methods return false indicating there are no constraints and no numbers in the cell
    private boolean setNum(int row, int col, int num){
        return !(
            rowCheck(row, num) || colCheck(col, num) || boxCheck(row, col, num)
        );
    }

    // Solve the sudoku
    public boolean runSolver(Solver solver){
        for(int r = 0; r < SIZE; r++){
            for(int c = 0; c < SIZE; c++){
                if(solution[r][c] == 0){
                    // check for numbers in range
                    for(int num = 1; num <= SIZE; num++){
                        solver.run();
                        if(setNum(r,c,num)){
                            // set number if it passes all checks
                            solution[r][c] = num;
                            // backtrack if we run into constraints, other wise return true to finish sudoku
                            if(runSolver(solver)){
                                return true;
                            } else {
                                // reset the cell to 0 and backtrack
                                solution[r][c] = 0;
                            }
                        }
                    }
                    return false;
                }
            }
        }
        // true if solved
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


   
    
