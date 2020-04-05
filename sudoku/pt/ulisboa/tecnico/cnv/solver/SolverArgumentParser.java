package pt.ulisboa.tecnico.cnv.solver;

import org.apache.commons.cli.Option;
import org.json.JSONArray;
import org.json.JSONException;
import pt.ulisboa.tecnico.cnv.util.AbstractArgumentParser;


public class SolverArgumentParser extends AbstractArgumentParser {
    public enum SolverParameters {
        /**
         * Set debug mode.
         */

        INPUT_SHORT("i"), INPUT("input"),
        PUZZLE_BOARD_SHORT("b"), PUZZLE_BOARD("board"),
        STRATEGY_SHORT("s"), STRATEGY("strategy"),
        UNASSIGNED_SHORT("un"), UNASSIGNED("unassigned"),

        INPUT_NR_COL("n1"),
        INPUT_NR_LIN("n2");

        private final String text;
        SolverParameters(final String text) {
            this.text = text;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return this.text;
        }
    }

    @Override
    public void parseValues(String[] args) {

        final String inputStr = cmd.getOptionValue(SolverParameters.INPUT.toString());
        if(inputStr == null || inputStr.trim().equals("")){
            throw new IllegalArgumentException(SolverParameters.INPUT.toString() + " is empty.");
        }
        super.argValues.put(SolverParameters.INPUT.toString(), inputStr);

        //BOARD
        final String inputBoard = cmd.getOptionValue(SolverParameters.PUZZLE_BOARD.toString());
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(inputBoard);
        }catch (JSONException je){
            throw  new IllegalArgumentException(SolverParameters.PUZZLE_BOARD.toString() + " is not a valid JSON array: " + inputBoard+".");
        }
        if(jsonArray.length() < 9 || !(jsonArray.get(0) instanceof JSONArray) || jsonArray.getJSONArray(0).length() < 9)
            throw new IllegalArgumentException(SolverParameters.PUZZLE_BOARD.toString() + " must correspond to at least a 9x9 board: " + jsonArray.length()+"x"+jsonArray.getJSONArray(0).length()+".");

        if(jsonArray.length() != jsonArray.getJSONArray(0).length())
            throw  new IllegalArgumentException(SolverParameters.PUZZLE_BOARD.toString() + " must correspond to a regular board NxN: " + jsonArray.length()+"x"+jsonArray.getJSONArray(0).length()+".");

        super.argValues.put(SolverParameters.PUZZLE_BOARD.toString(), inputBoard);

        //N1
        final Integer n1;
        if(cmd.hasOption(SolverParameters.INPUT_NR_COL.toString())){
            n1 = Integer.parseInt(cmd.getOptionValue(SolverParameters.INPUT_NR_COL.toString()));
            if(n1 != jsonArray.length())
                throw new IllegalArgumentException(SolverParameters.INPUT_NR_COL.toString() + " must be equal to the number of columns of the given boards: " + jsonArray.length()+".");
            super.argValues.put(SolverParameters.INPUT_NR_COL.toString(), n1);
        }else{
            n1 = jsonArray.length();
            super.argValues.put(SolverParameters.INPUT_NR_COL.toString(), n1);
        }

        //N2
        final Integer n2;
        if(cmd.hasOption(SolverParameters.INPUT_NR_LIN.toString())){
            n2 = Integer.parseInt(cmd.getOptionValue(SolverParameters.INPUT_NR_LIN.toString()));
            if(n2 != jsonArray.getJSONArray(0).length())
                throw new IllegalArgumentException(SolverParameters.INPUT_NR_LIN.toString() + " must be equal to the number of lines of the given boards: " + jsonArray.length()+".");
            super.argValues.put(SolverParameters.INPUT_NR_LIN.toString(), n2);

        }else{
            n2 = jsonArray.getJSONArray(0).length();
            super.argValues.put(SolverParameters.INPUT_NR_LIN.toString(), n2);
        }

        //UN
        final Integer un = Integer.parseInt(cmd.getOptionValue(SolverParameters.UNASSIGNED.toString()));
        if(un < 0 || un > (n1*n2))
            throw new IllegalArgumentException(SolverParameters.UNASSIGNED.toString() + " must be a positive number smaller than " + (n1*n2)+".");
        super.argValues.put(SolverParameters.UNASSIGNED.toString(), un);


        // Validate the chosen solver strategy.
        if(cmd.hasOption(SolverArgumentParser.SolverParameters.STRATEGY.toString())) {
            final String strategy = cmd.getOptionValue(SolverArgumentParser.SolverParameters.STRATEGY.toString());

            if( ! SolverFactory.SolverType.isValid(strategy)) {
                throw new IllegalArgumentException(strategy + " is an invalid generator strategy.");
            }

            super.argValues.put(SolverArgumentParser.SolverParameters.STRATEGY.toString(), SolverFactory.SolverType.valueOf(strategy));
        }

    }

    @Override
    public void setupCLIOptions() {

        // Mandatory arguments.

        final Option inputStrOption = new Option(SolverParameters.INPUT_SHORT.toString(), SolverParameters.INPUT.toString(),true, "name of the sudoku board.");
        inputStrOption.setRequired(true);
        super.options.addOption(inputStrOption);

        final Option inputBoardOption = new Option(SolverParameters.PUZZLE_BOARD_SHORT.toString(), SolverParameters.PUZZLE_BOARD.toString(),true, "JSON array of sudoku board.");
        inputBoardOption.setRequired(true);
        super.options.addOption(inputBoardOption);

        final Option strategyOption = new Option(SolverParameters.STRATEGY_SHORT.toString(), SolverParameters.STRATEGY.toString(),
                true, "solver strategy can be one of: BFS, DLX or CP.");
        strategyOption.setRequired(true);
        super.options.addOption(strategyOption);

        final Option unassignedOption = new Option(SolverParameters.UNASSIGNED_SHORT.toString(), SolverParameters.UNASSIGNED.toString(),
                true, "number of empty entries in the board.");
        unassignedOption.setRequired(true);
        super.options.addOption(unassignedOption);


        //// Sudoku Board Size.

        final Option x0Option = new Option(SolverParameters.INPUT_NR_COL.toString(),true, "number of columns of sudoku board (default 9).");
        x0Option.setRequired(false);
        super.options.addOption(x0Option);

        final Option y0Option = new Option(SolverParameters.INPUT_NR_LIN.toString(),true, "number of lines of sudoku board  (default 9).");
        y0Option.setRequired(false);
        super.options.addOption(y0Option);

   /*     final Option x1Option = new Option(SolverParameters.LOWER_RIGHT_X.toString(),true, "lower-right x coordinate (default equal to image width).");
        x1Option.setRequired(false);
        super.options.addOption(x1Option);*/

    }

    public SolverArgumentParser(final String[] args) {
        this.setup(args);
    }

    public Integer getN1(){
        return (Integer)super.argValues.get(SolverParameters.INPUT_NR_COL.toString());

    }
    public Integer getN2(){
        return (Integer)super.argValues.get(SolverParameters.INPUT_NR_LIN.toString());

    }

    public Integer getUn(){
        return (Integer)super.argValues.get(SolverParameters.UNASSIGNED.toString());

    }

    public String getInputBoard() {
        return (String) super.argValues.get(SolverParameters.INPUT.toString());

    }


    public String getPuzzleBoard() {
        return (String) super.argValues.get(SolverParameters.PUZZLE_BOARD.toString());

    }

    public SolverFactory.SolverType getSolverStrategy() {
        return (SolverFactory.SolverType)super.argValues.get(SolverArgumentParser.SolverParameters.STRATEGY.toString());
    }
}
