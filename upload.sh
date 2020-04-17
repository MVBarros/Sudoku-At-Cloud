rm graphics out/* README* BIT/samples/*.class BIT/examples/out/*.class BIT/examples/*.class -r

scp -r -i "aws/CNV-Project-Pair.pem" .  ec2-user@ec2-3-84-127-219.compute-1.amazonaws.com:~ec2-user

git stash
