rm sudoku/pt/ulisboa/tecnico/cnv/server/*.class
rm sudoku/pt/ulisboa/tecnico/cnv/server/task/*.class
rm sudoku/pt/ulisboa/tecnico/cnv/solver/*.class
rm BIT/samples/metrics/tools/*.class
rm BIT/BIT/highBIT/*.class
rm BIT/BIT/lowBIT/*.class
rm dynamo/src/pt/ulisboa/tecnico/cnv/dynamo/*.class
rm loadbalancer/src/pt/ulisboa/tecnico/cnv/loadbalancer/*/*.class
rm loadbalancer/src/pt/ulisboa/tecnico/cnv/loadbalancer/*.class
rm autoscaler/src/pt/ulisboa/tecnico/cnv/autoscaler/*.class
rm autoscaler/src/pt/ulisboa/tecnico/cnv/autoscaler/*.class
zip -x "*.html" -r out sudoku/ BIT/samples BIT/BIT/ dynamo loadbalancer autoscaler compile-aws.sh java-config-aws.sh aws-java-sdk-1.11.779 credentials
