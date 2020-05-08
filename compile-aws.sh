source /home/ec2-user/Sudoku-At-Cloud/java-config-aws.sh
javac /home/ec2-user/Sudoku-At-Cloud/sudoku/pt/ulisboa/tecnico/cnv/solver/*.java
javac /home/ec2-user/Sudoku-At-Cloud/BIT/samples/metrics/tools/*.java
java metrics.tools.SudokuMetricsBFS /home/ec2-user/Sudoku-At-Cloud/sudoku/pt/ulisboa/tecnico/cnv/solver/SudokuSolverBFS.class
java metrics.tools.SudokuMetricsDLX /home/ec2-user/Sudoku-At-Cloud/sudoku/pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX.class
java metrics.tools.SudokuMetricsCP /home/ec2-user/Sudoku-At-Cloud/sudoku/pt/ulisboa/tecnico/cnv/solver/SudokuSolverCP.class
java metrics.tools.SudokuMetricsDLX '/home/ec2-user/Sudoku-At-Cloud/sudoku/pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver.class'
java metrics.tools.SudokuMetricsDLX '/home/ec2-user/Sudoku-At-Cloud/sudoku/pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$Node.class'
java metrics.tools.SudokuMetricsDLX '/home/ec2-user/Sudoku-At-Cloud/sudoku/pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$ColumnNode.class'
java metrics.tools.SudokuMetricsDLX '/home/ec2-user/Sudoku-At-Cloud/sudoku/pt/ulisboa/tecnico/cnv/solver/SudokuSolverDLX$AlgorithmXSolver$ColumnID.class'
javac /home/ec2-user/Sudoku-At-Cloud/sudoku/pt/ulisboa/tecnico/cnv/server/*.java
java pt.ulisboa.tecnico.cnv.server.WebServer

