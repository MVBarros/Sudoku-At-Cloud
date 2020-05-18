source java-config-rnl.sh

javac loadbalancer/src/pt/ulisboa/tecnico/cnv/loadbalancer/*.java
javac loadbalancer/src/pt/ulisboa/tecnico/cnv/loadbalancer/*/*.java
javac autoscaler/src/pt/ulisboa/tecnico/cnv/autoscaler/*.java
javac autoscaler/src/pt/ulisboa/tecnico/cnv/autoscaler/*/*.java
java pt.ulisboa.tecnico.cnv.loadbalancer.Main
