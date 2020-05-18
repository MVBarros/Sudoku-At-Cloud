rm sudoku/pt/ulisboa/tecnico/cnv/server/*.class
rm sudoku/pt/ulisboa/tecnico/cnv/server/task/*.class
rm sudoku/pt/ulisboa/tecnico/cnv/solver/*.class
rm BIT/samples/metrics/tools/*.class
rm BIT/BIT/highBIT/*.class
rm BIT/BIT/lowBIT/*.class
rm loadbalancer/src/pt/ulisboa/tecnico/cnv/loadbalancer/*/*.class
rm loadbalancer/src/pt/ulisboa/tecnico/cnv/loadbalancer/*.class
rm dynamo/src/pt/ulisboa/tecnico/cnv/dynamo/*.class
zip -x "*.html" -r out sudoku/ BIT/samples BIT/BIT/ dynamo compile-aws.sh java-config-aws.sh aws-java-sdk-1.11.779 credentials loadbalancer
