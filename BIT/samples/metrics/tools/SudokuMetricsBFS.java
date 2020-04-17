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
            else if (opcode == InstructionTable.putfield)
                instr.addBefore("metrics/tools/SudokuMetricsTool", "storeField", "null");
            else {
                short instr_type = InstructionTable.InstructionTypeTable[opcode];
                if (instr_type == InstructionTable.LOAD_INSTRUCTION) {
                    instr.addBefore("metrics/tools/SudokuMetricsTool", "load", "null");
                } else if (instr_type == InstructionTable.STORE_INSTRUCTION) {
                    instr.addBefore("metrics/tools/SudokuMetricsTool", "store", "null");
                }
            }
        }
    }

    public static void doBranch(InstructionArray instructions) {
        for (Enumeration instrs = instructions.elements(); instrs.hasMoreElements(); ) {
            Instruction instr = (Instruction) instrs.nextElement();
            short instr_type = InstructionTable.InstructionTypeTable[instr.getOpcode()];
            if (instr_type == InstructionTable.CONDITIONAL_INSTRUCTION) {
                instr.addBefore("metrics/tools/SudokuMetricsTool", "updateBranch", "null");
            }
        }
    }

    public static void doInstr(Routine routine, Enumeration blocks) {
        routine.addBefore("metrics/tools/SudokuMetricsTool", "method", 1);
        for (Enumeration b = blocks; b.hasMoreElements(); ) {
            BasicBlock bb = (BasicBlock) b.nextElement();
            bb.addBefore("metrics/tools/SudokuMetricsTool", "instructions", bb.size());
        }
    }

    public static void doStackDepth(Routine routine) {
        routine.addBefore("metrics/tools/SudokuMetricsTool", "addStackDepth", new Integer(routine.getMaxStack()));
        routine.addAfter("metrics/tools/SudokuMetricsTool", "removeStackDepth", new Integer(routine.getMaxStack()));
    }

    public static void addInstrumentation(File in_file) {
        String filename = in_file.getName();

            if (filename.endsWith(".class")) {
                String in_filename = in_file.getAbsolutePath() + System.getProperty("file.separator") + filename;
                ClassInfo ci = new ClassInfo(in_filename);
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    InstructionArray instructions = routine.getInstructionArray();
                    Enumeration blocks = routine.getBasicBlocks().elements();
                    doLoadStore(instructions);
                    doBranch(instructions);
                    doInstr(routine, blocks);
                    doStackDepth(routine);
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



    public static void instructions(int incr) {
        StatsBFS stats = getCurrentStats();
        stats.incrInstructionCount(incr);
        stats.incrBasicBlockCount();
    }

    public static void method(int foo) {
        getCurrentStats().incrMethodCount();
    }


    public static void addStackDepth(int depth) {
        getCurrentStats().incrCurrStackDepth(depth);
    }

    public static void removeStackDepth(int dept) {
        getCurrentStats().decrCurrStackDepth(dept);
    }

    public static void load(String foo) {
        getCurrentStats().incrLoadCount();
    }

    public static void loadField(String foo) {
        getCurrentStats().incrFieldLoadCount();
    }

    public static void store(String foo) {
        getCurrentStats().incrStoreCount();
    }

    public static void storeField(String foo) {
        getCurrentStats().incrFieldStoreCount();
    }

    public static void updateBranch(String foo) {
        getCurrentStats().incrBranchCount();
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

