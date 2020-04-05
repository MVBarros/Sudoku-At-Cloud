package pt.ulisboa.tecnico.cnv.solver;

public class SolverFactory {

    public enum SolverType {
        BFS("BFS"),
        DLX("DLX"),
        CP("CP");

        private final String text;
        SolverType(final String text) {
            this.text = text;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return text;
        }

        static public Boolean isValid(final String strategyString) {

            for (SolverType enumStrat : SolverType.values()) {
                if(enumStrat.toString().equals(strategyString)) {
                    return true;
                }
            }
            return false;
        }
    }

    // Singleton.
    static private SolverFactory instance = null;
    private SolverFactory(){}
    static public SolverFactory getInstance() {
        if(instance == null) {
            instance = new SolverFactory();
        }
        return instance;
    }

    public Solver makeSolver(final SolverArgumentParser ap) {

        final SolverType t = ap.getSolverStrategy();

        if(t == SolverType.BFS) {
            return new Solver(ap, new SudokuSolverBFS());
        }
        else if(t == SolverType.DLX) {
            return new Solver(ap, new SudokuSolverDLX());
        }
        else if(t == SolverType.CP) {
            return new Solver(ap, new SudokuSolverCP());
        }
        else {
            throw new IllegalArgumentException();
        }
    }
}
