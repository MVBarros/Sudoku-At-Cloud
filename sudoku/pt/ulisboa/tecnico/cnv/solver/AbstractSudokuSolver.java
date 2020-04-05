package pt.ulisboa.tecnico.cnv.solver;


public abstract class AbstractSudokuSolver implements SudokuSolver{

    protected String name;

    static int SIZE;
    static int BOX_SIZE;

    int[][] puzzle;
    int[][] solution;

    public AbstractSudokuSolver() {
    }

    public AbstractSudokuSolver(int[][] puzzle) {
        setPuzzle(puzzle);
    }
    @Override
    public String toString() {
        return this.name;
    }

    public void setPuzzle(int[][] puzzle) {
        SIZE = puzzle.length;
        BOX_SIZE = (int) Math.sqrt(SIZE);
        this.puzzle = puzzle;
        this.solution = deepCloneArray(puzzle);
    }

    public abstract boolean runSolver(Solver solver);

    public int[][] getPuzzle() {
        return puzzle;
    }

    public int[][] getSolution() {
        return solution;
    }

    public void printPuzzle(){
        System.out.println("----- PUZZLE -----");
        print(puzzle);
    }

    public void printSolution(){
        System.out.println("----- SOLUTION -----");
        print(solution);
    }


    /* The print() function outputs the SudokuSolverDLX grid to the standard output, using
     * a bit of extra formatting to make the result clearly readable. */
    private void print(int[][] board)
    {
        // Compute the number of digits necessary to print out each number in the SudokuSolverDLX puzzle
        int digits = (int) Math.floor(Math.log(SIZE) / Math.log(10)) + 1;

        // Create a dashed line to separate the boxes
        int lineLength = (digits + 1) * SIZE + 2 * BOX_SIZE - 3;
        StringBuffer line = new StringBuffer();
        for( int lineInit = 0; lineInit < lineLength; lineInit++ )
            line.append('-');

        // Go through the solution, printing out its values separated by spaces
        for(int i = 0; i < SIZE; i++ ) {
            for(int j = 0; j < SIZE; j++ ) {
                printFixedWidth( String.valueOf( board[i][j] ), digits );
                // Print the vertical lines between boxes
                if( (j < SIZE -1) && ((j+1) % BOX_SIZE == 0) )
                    System.out.print( " |" );
                System.out.print( " " );
            }
            System.out.println();

            // Print the horizontal line between boxes
            if( (i < SIZE -1) && ((i+1) % BOX_SIZE == 0) )
                System.out.println( line.toString() );
        }
        System.out.println("");
    }

    /* Helper function for the printing of SudokuSolverDLX puzzle.  This function will print
     * out text, preceded by enough ' ' characters to make sure that the printint out
     * takes at least width characters.  */
    private void printFixedWidth( String text, int width )
    {
        for( int i = 0; i < width - text.length(); i++ )
            System.out.print( " " );
        System.out.print( text );
    }

    /**
     * Creates an independent copy(clone) of an array.
     *
     * @param array The array to be cloned.
     * @return An independent 'deep' structure clone of the array.
     */
    public static int[][] deepCloneArray(int[][] array) {
        int rows = array.length;

        //clone the 'shallow' structure of array
        int[][] newArray = (int[][]) array.clone();
        //clone the 'deep' structure of array
        for (int row = 0; row < rows; row++) {
            newArray[row] = (int[]) array[row].clone();
        }

        return newArray;
    }
}
