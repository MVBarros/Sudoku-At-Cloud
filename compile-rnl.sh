source java-config-rnl.sh
javac sudoku/pt/ulisboa/tecnico/cnv/solver/*.java
javac BIT/samples/metrics/tools/*.java
java metrics.tools.SudokuMetricsBFS sudoku/pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS.class
java metrics.tools.SudokuMetricsDLX sudoku/pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX.class
java metrics.tools.SudokuMetricsCP sudoku/pt/ulisboa/tecnico/cnv/solver/SudokuSolverCP.class
java metrics.tools.SudokuMetricsDLX 'sudoku/pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver.class'
java metrics.tools.SudokuMetricsDLX 'sudoku/pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$Node.class'
java metrics.tools.SudokuMetricsDLX 'sudoku/pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$ColumnNode.class'
java metrics.tools.SudokuMetricsDLX 'sudoku/pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$ColumnID.class'
javac sudoku/pt/ulisboa/tecnico/cnv/server/*.java
java pt.ulisboa.tecnico.cnv.server.WebServer

