source /home/ec2-user/Sudoku-At-Cloud/java-config.sh
javac /home/ec2-user/Sudoku-At-Cloud/sudoku/pt/ulisboa/tecnico/cnv/solver/*.java
javac /home/ec2-user/Sudoku-At-Cloud/BIT/samples/*.java
java SudokuMetricsTool /home/ec2-user/Sudoku-At-Cloud/sudoku/pt/ulisboa/tecnico/cnv/solver
javac /home/ec2-user/Sudoku-At-Cloud/sudoku/pt/ulisboa/tecnico/cnv/server/*.java
java pt.ulisboa.tecnico.cnv.server.WebServer

