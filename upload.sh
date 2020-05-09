rm out.zip
./mkzip.sh
scp -r -i aws/CNV-Project-Pair.pem out.zip unzip.sh $1:
