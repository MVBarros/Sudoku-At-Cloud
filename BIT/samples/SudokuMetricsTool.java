import BIT.highBIT.*;
import org.json.JSONObject;
import pt.ulisboa.tecnico.cnv.server.WebServer;
import pt.ulisboa.tecnico.cnv.server.MetricUtils;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SudokuMetricsTool {

    private static final ConcurrentHashMap<String, Stats> stats = new ConcurrentHashMap<>();

    public static String getCurrentThreadName() {
        return String.valueOf(Thread.currentThread().getId());
    }

    private static Stats getCurrentStats() {
        String name = getCurrentThreadName();
        stats.putIfAbsent(name, new Stats());
        return stats.get(name);
    }

    public static void printUsage() {
        System.out.println("Syntax: java SudokuMetricsTool in_path ");
        System.out.println();
        System.out.println("        in_path:  directory from which the class files are read and to which they are written");
        System.exit(-1);
    }


    public static void doAlloc(InstructionArray instructions) {
        for (Enumeration instrs = instructions.elements(); instrs.hasMoreElements(); ) {
            Instruction instr = (Instruction) instrs.nextElement();
            int opcode = instr.getOpcode();
            switch (opcode) {
                case InstructionTable.NEW:
                    instr.addBefore("SudokuMetricsTool", "allocNew", "null");
                    break;
                case InstructionTable.newarray:
                    instr.addBefore("SudokuMetricsTool", "allocNewArray", "null");
                    break;
                case InstructionTable.anewarray:
                    instr.addBefore("SudokuMetricsTool", "allocANewArray", "null");
                    break;
                case InstructionTable.multianewarray:
                    instr.addBefore("SudokuMetricsTool", "allocMultiNewArray", "null");
                    break;
            }
        }
    }

    public static void doLoadStore(InstructionArray instructions) {
        for (Enumeration instrs = instructions.elements(); instrs.hasMoreElements(); ) {
            Instruction instr = (Instruction) instrs.nextElement();
            int opcode = instr.getOpcode();
            if (opcode == InstructionTable.getfield)
                instr.addBefore("SudokuMetricsTool", "loadField", "null");
            else if (opcode == InstructionTable.putfield)
                instr.addBefore("SudokuMetricsTool", "storeField", "null");
            else {
                short instr_type = InstructionTable.InstructionTypeTable[opcode];
                if (instr_type == InstructionTable.LOAD_INSTRUCTION) {
                    instr.addBefore("SudokuMetricsTool", "load", "null");
                } else if (instr_type == InstructionTable.STORE_INSTRUCTION) {
                    instr.addBefore("SudokuMetricsTool", "store", "null");
                }
            }
        }
    }

    public static void doBranch(InstructionArray instructions) {
        for (Enumeration instrs = instructions.elements(); instrs.hasMoreElements(); ) {
            Instruction instr = (Instruction) instrs.nextElement();
            short instr_type = InstructionTable.InstructionTypeTable[instr.getOpcode()];
            if (instr_type == InstructionTable.CONDITIONAL_INSTRUCTION) {
                instr.addBefore("SudokuMetricsTool", "updateBranch", "null");
            }
        }
    }

    public static void doInstr(Routine routine, Enumeration blocks) {
        routine.addBefore("SudokuMetricsTool", "method", 1);
        for (Enumeration b = blocks; b.hasMoreElements(); ) {
            BasicBlock bb = (BasicBlock) b.nextElement();
            bb.addBefore("SudokuMetricsTool", "instructions", bb.size());
        }
    }

    public static void doStackDepth(Routine routine) {
        routine.addBefore("SudokuMetricsTool", "addStackDepth", routine.getMaxStack());
        routine.addAfter("SudokuMetricsToo", "removeStackDepth", routine.getMaxStack());
    }

    public static void doCallback(Routine routine) {
        if (routine.getMethodName().equals("solveSudoku")) {
            routine.addAfter("SudokuMetricsTool", "saveStats", "null");
        }
    }

    public static void addInstrumentation(File in_dir) {
        String[] fileList = in_dir.list();

        for (String filename : fileList) {
            if (filename.endsWith(".class")) {
                String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                ClassInfo ci = new ClassInfo(in_filename);
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    doStackDepth(routine);
                    doAlloc(routine.getInstructionArray());
                    doLoadStore(routine.getInstructionArray());
                    doBranch(routine.getInstructionArray());
                    doInstr(routine, routine.getBasicBlocks().elements());
                    doCallback(routine);
                }
                ci.write(in_filename);
            }
        }
    }

    public static void saveStats(String foo) {
        SolverArgumentParser parser = WebServer.getCurrentThreadBoard();
        writeToFile(getCurrentStats(), parser);
        //Thread is effectively finished after SolveSudoku
        stats.remove(getCurrentThreadName());
    }

    public static void writeToFile(Stats stats, SolverArgumentParser parser) {
        JSONObject object = stats.toJSON();
        object.put("Board", MetricUtils.toJSON(parser));
        String outputDir = "./out";
        String path = outputDir + System.getProperty("file.separator") + UUID.randomUUID().toString() + ".json";
        try (PrintWriter out = new PrintWriter(path)) {
            out.println(object.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void instructions(int incr) {
        Stats stats = getCurrentStats();
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
        getCurrentStats().incrCurrStackDepth(-dept);
    }

    public static void allocNew(String foo) {
        getCurrentStats().incrNewCount();
    }

    public static void allocNewArray(String foo) {
        getCurrentStats().incrNewArrayCount();
    }

    public static void allocMultiNewArray(String foo) {
        getCurrentStats().incrMultiANewArrayCount();
    }

    public static void allocANewArray(String foo) {
        getCurrentStats().incrANewArrayCount();
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
            File in_dir = new File(argv[0]);

            if (in_dir.isDirectory()) {
                addInstrumentation(in_dir);
            } else {
                printUsage();
            }
        } catch (NullPointerException e) {
            printUsage();
        }
    }
}

