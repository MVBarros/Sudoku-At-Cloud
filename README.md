# Sudoku-At-Cloud

## Group 14

87675 João Rafael Pinto Soares

87691 Miguel Veloso Barros

94112 Gonçalo Miguel Teixeira Estevinho Pires

# LOAD BALANCER

Ping Protocol - HTTP

Pint Port - 8000

Ping Path - /lb

Response Timeout - 5 seconds

Interval - 30 seconds

Unealthy threshold - 3

Healthy threshold - 10

# Scaling Policy

## Decrease Group Size:

Execute policy when: CPUUtilization >= 40 for 300 seconds

Take the action: Remove 1 capacity units when 40 <= CPUUtilization < +infinity

## Increase Group Size:

CPUUtilization >= 80 for 300 seconds

Add 1 capacity unit when 80 <= CPUUtilization <= + infinity 

Instances need 30 seconds to warm up after each step


Grace Period: 30 seconds

Minimum instances: 1

Maximum instances: 5
