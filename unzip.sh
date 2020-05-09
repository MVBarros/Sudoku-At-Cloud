mkdir Sudoku-At-Cloud
unzip out.zip -d Sudoku-At-Cloud
mkdir ~/.aws
mv Sudoku-At-Cloud/credentials ~/.aws/
sudo cp ~ec2-user/.aws/credentials ~root/.aws
