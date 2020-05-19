source /home/ec2-user/Sudoku-At-Cloud/java-config-aws.sh
javac /home/ec2-user/Sudoku-At-Cloud/sudoku/pt/ulisboa/tecnico/cnv/solver/*.java
javac /home/ec2-user/Sudoku-At-Cloud/BIT/samples/metrics/tools/*.java
java metrics.tools.SudokuMetricsTool /home/ec2-user/Sudoku-At-Cloud/sudoku/pt/ulisboa/tecnico/cnv/solver/
javac /home/ec2-user/Sudoku-At-Cloud/dynamo/src/pt/ulisboa/tecnico/cnv/dynamo/*.java
javac /home/ec2-user/Sudoku-At-Cloud/dynamo/src/pt/ulisboa/tecnico/cnv/dynamo/cache/*.java
javac /home/ec2-user/Sudoku-At-Cloud/sudoku/pt/ulisboa/tecnico/cnv/server/*.java
java pt.ulisboa.tecnico.cnv.server.WebServer
