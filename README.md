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

Execute policy when: CPUUtilization >= 30 for 180 seconds

Take the action: Remove 1 capacity units when 30 <= CPUUtilization < +infinity

## Increase Group Size:

CPUUtilization >= 60 for 180 seconds

Add 1 capacity unit when 60 <= CPUUtilization <= + infinity 

Instances need 30 seconds to warm up after each step

Grace Period: 30 seconds

Minimum instances: 1

Maximum instances: 5
