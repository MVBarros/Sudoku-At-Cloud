package pt.ulisboa.tecnico.cnv.solver;

public class SampleParserMain {
    public static void main(final String[] args) {
        // Get user-provided flags.
        final SolverArgumentParser ap = new SolverArgumentParser(args);

        // Specified board size.
        int N1 = ap.getN1();
        int N2 = ap.getN2();
        System.out.println(String.format("> Sudoku board size:\t\t(%d,% d)", N1, N2));

        // Unassigned entries.
        int Un = ap.getUn();
        System.out.println(String.format("> Unassigned entries:\t%d", Un));


        // Get the strategy type.
        String st = ap.getSolverStrategy().toString();
        System.out.println("> Search strategy:\t\t" + st);


        // Get the strategy type.
        String input = ap.getInputBoard().toString();
        System.out.println("> Input:\t\t" + input);

        // Get input map.
        String board = ap.getPuzzleBoard();
        System.out.println("> Board:\t\t\t" + board);
    }
}
