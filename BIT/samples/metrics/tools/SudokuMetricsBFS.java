package metrics.tools;

import BIT.highBIT.*;
import org.json.JSONObject;
import pt.ulisboa.tecnico.cnv.server.MetricUtils;
import pt.ulisboa.tecnico.cnv.server.WebServer;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SudokuMetricsBFS {

    private static final ConcurrentHashMap<Long, StatsBFS> stats = new ConcurrentHashMap<>();

    public static long getCurrentThreadId() {
        return Thread.currentThread().getId();
    }

    private static StatsBFS getCurrentStats() {
        //no thread tries to access the same key at the same time
        Long id = getCurrentThreadId();
        StatsBFS stat = stats.get(id);
        if (stat == null) {
            stat = new StatsBFS();
            stats.put(id, stat);
        }
        return stat;
    }

    public static void printUsage() {
        System.out.println("Syntax: java SudokuMetricsBFS in_file ");
        System.out.println();
        System.out.println("        in_file:  file from which the class files are read and to which they are written");
        System.exit(-1);
    }


    public static void doLoadStore(InstructionArray instructions) {
        for (Enumeration instrs = instructions.elements(); instrs.hasMoreElements(); ) {
            Instruction instr = (Instruction) instrs.nextElement();
            int opcode = instr.getOpcode();
            if (opcode == InstructionTable.getfield)
                instr.addBefore("metrics/tools/SudokuMetricsTool", "loadField", "null");
            else {
                short instr_type = InstructionTable.InstructionTypeTable[opcode];
                if (instr_type == InstructionTable.STORE_INSTRUCTION) {
                    instr.addBefore("metrics/tools/SudokuMetricsTool", "store", "null");
                }
            }
        }
    }

    public static void doRoutine(Routine routine) {
        routine.addBefore("metrics/tools/SudokuMetricsBFS", "method", 1);
    }


    public static void addInstrumentation(File in_file) {
        String filename = in_file.getName();

        if (filename.endsWith(".class")) {
            String in_filename = in_file.getAbsolutePath() + System.getProperty("file.separator") + filename;
            ClassInfo ci = new ClassInfo(in_filename);
            for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                Routine routine = (Routine) e.nextElement();
                InstructionArray instructions = routine.getInstructionArray();
                doLoadStore(instructions);
                doRoutine(routine);
            }
            ci.write(in_filename);
        }
    }

    public static void saveStats() {
        SolverArgumentParser parser = WebServer.getCurrentThreadBoard();
        writeToFile(getCurrentStats(), parser);
        //Thread is effectively finished after SolveSudoku and will not call more solver code
        stats.remove(getCurrentThreadId());
    }

    public static void writeToFile(Stats stats, SolverArgumentParser parser) {
        JSONObject object = stats.toJSON();
        object.put("Board", MetricUtils.toJSON(parser));

        String outputDir = "out";

        String strat = parser.getSolverStrategy().toString();
        String name = parser.getInputBoard();
        String un = parser.getUn().toString();
        name += "-" + strat + "-" + un + "-" + UUID.randomUUID().toString();

        String path = outputDir + System.getProperty("file.separator") + name + ".json";
        try (PrintWriter out = new PrintWriter(path)) {
            out.println(object.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void method(int foo) {
        getCurrentStats().incrMethodCount();
    }

    public static void loadField(String foo) {
        getCurrentStats().incrFieldLoadCount();
    }

    public static void store(String foo) {
        getCurrentStats().incrStoreCount();
    }


    public static void main(String[] argv) {
        if (argv.length != 1) {
            printUsage();
        }
        try {
            File in_file = new File(argv[0]);
            if (in_file.exists()) {
                addInstrumentation(in_file);
            } else {
                printUsage();
            }
        } catch (NullPointerException e) {
            printUsage();
        }
    }
}

