# Sudoku-At-Cloud

## Group 14

87675 João Rafael Pinto Soares

87691 Miguel Veloso Barros

94112 Gonçalo Miguel Teixeira Estevinho Pires

## Organization

autoscaler - Autoscaler java module, responsible for monitoring the cluster execution and creating/terminating instances

aws - AWS Private Key file to ssh with the instances

BIT - BIT tool and Instrumentation classes used (in BIT/samples/metrics/tool)

docs - Project specification and reports

dynamo - Dynamo java module, responsible for interacting with dynamoDB (writing and reading to the DB)

loadbalancer - load balancer module, HTTP Server that redirects requests to instances and keeps instances with roughly the same work

sudoku - Sudoku Solving algorithms and WebServer to be contacted with Sudoku problems

compile scripts - compiles and executes a Sudoku Web Server on port 8000(one script for aws and one script for the rnl vm)

compile-lb scripts - compiles and executes a Load Balancer server on port 8000(one script for aws and one script for the rnl vm)

mkzip script - creates a zip of the sudoku module and it's dependencies 

mkziplb script - create a zip of the load balancer module and it's dependencies

unzip script - unzips the zip created by the mkzip scripts and sets the credentials (to use in aws only, works only with the root account)

java config scripts - configures Java 7 and the classpath (one for aws and one for the rnl vm)

credentials - AWS Credentials file to be able to use the AWS SDK

all scripts assume you have the aws sdk in this directory with version 1.11.779 which we were not able to include due to size constraints, can be downloaded [here](http://web.tecnico.ulisboa.pt/ist187691/aws-java-sdk-1.11.779.zip)

