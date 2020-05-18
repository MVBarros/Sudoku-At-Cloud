source /home/ec2-user/Sudoku-At-Cloud/java-config-aws.sh
javac /home/ec2-user/Sudoku-At-Cloud/loadbalancer/src/pt/ulisboa/tecnico/cnv/loadbalancer/*.java
javac /home/ec2-user/Sudoku-At-Cloud/loadbalancer/src/pt/ulisboa/tecnico/cnv/loadbalancer/*/*.java
javac /home/ec2-user/Sudoku-At-Cloud/autoscaler/src/pt/ulisboa/tecnico/cnv/autoscaler/*.java
javac /home/ec2-user/Sudoku-At-Cloud/autoscaler/src/pt/ulisboa/tecnico/cnv/autoscaler/*/*.java
java pt.ulisboa.tecnico.cnv.loadbalancer.Main
