source java-config-rnl.sh
javac sudoku/pt/ulisboa/tecnico/cnv/solver/*.java
javac BIT/samples/metrics/tools/*.java
java metrics.tools.SudokuMetricsTool sudoku/pt/ulisboa/tecnico/cnv/solver/
javac sudoku/pt/ulisboa/tecnico/cnv/server/*.java
java pt.ulisboa.tecnico.cnv.server.WebServer

