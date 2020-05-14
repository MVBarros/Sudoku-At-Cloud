package pt.ulisboa.tecnico.cnv.loadbalancer.sudoku;

public class SudokuParametersBuilder {
        private  int n1;
        private  int n2;
        private  int un;
        private  String inputBoard;
        private  String puzzleBoard;
        private  SudokuParameters.Strategy strategy;

        SudokuParametersBuilder() {}

        public SudokuParametersBuilder setN1(int n1) {
            this.n1 = n1;
            return this;
        }

        public SudokuParametersBuilder setN2(int n2) {
            this.n2 = n2;
            return this;
        }

        public SudokuParametersBuilder setUn(int un) {
            this.un = un;
            return this;
        }

        public SudokuParametersBuilder setInputBoard(String inputBoard) {
            this.inputBoard = inputBoard;
            return this;
        }

        public SudokuParametersBuilder setPuzzleBoard(String puzzleBoard) {
            this.puzzleBoard = puzzleBoard;
            return this;
        }

        public SudokuParametersBuilder setStrategy(String strategy) {
            this.strategy = SudokuParameters.Strategy.valueOf(strategy);
            return this;
        }

        public SudokuParameters build() {
            return new SudokuParameters(n1, n2, un, inputBoard, puzzleBoard, strategy);
        }
}
