import BIT.highBIT.*;
import org.json.JSONObject;
import pt.ulisboa.tecnico.cnv.server.WebServer;
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

    public static void addInstructionInstrumentation(File in_dir) {
        String[] fileList = in_dir.list();

        for (String filename : fileList) {
            if (filename.endsWith(".class")) {
                String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                ClassInfo ci = new ClassInfo(in_filename);
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    routine.addBefore("SudokuMetricsTool", "method", 1);
                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        bb.addBefore("SudokuMetricsTool", "instr", bb.size());
                    }
                }
                ci.write(in_filename);
            }
        }
    }

    public static void addAlocationInstrumentation(File in_dir) {
        String[] fileList = in_dir.list();

        for (String filename : fileList) {
            if (filename.endsWith(".class")) {
                String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                ClassInfo ci = new ClassInfo(in_filename);
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    InstructionArray instructions = routine.getInstructionArray();
                    for (Enumeration instrs = instructions.elements(); instrs.hasMoreElements(); ) {
                        Instruction instr = (Instruction) instrs.nextElement();
                        int opcode = instr.getOpcode();
                        if ((opcode == InstructionTable.NEW) ||
                                (opcode == InstructionTable.newarray) ||
                                (opcode == InstructionTable.anewarray) ||
                                (opcode == InstructionTable.multianewarray)) {
                            instr.addBefore("SudokuMetricsTool", "alloc", opcode);
                        }
                    }
                }
                ci.write(in_filename);
            }
        }
    }

    public static void addLoadStoreInstrumentation(File in_dir) {
        String[] fileList = in_dir.list();

        for (String filename : fileList) {
            if (filename.endsWith(".class")) {
                String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                ClassInfo ci = new ClassInfo(in_filename);
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    for (Enumeration instrs = (routine.getInstructionArray()).elements(); instrs.hasMoreElements(); ) {
                        Instruction instr = (Instruction) instrs.nextElement();
                        int opcode = instr.getOpcode();
                        if (opcode == InstructionTable.getfield)
                            instr.addBefore("SudokuMetricsTool", "loadStoreField", 0);
                        else if (opcode == InstructionTable.putfield)
                            instr.addBefore("SudokuMetricsTool", "loadStoreField", 1);
                        else {
                            short instr_type = InstructionTable.InstructionTypeTable[opcode];
                            if (instr_type == InstructionTable.LOAD_INSTRUCTION) {
                                instr.addBefore("SudokuMetricsTool", "loadStore", 0);
                            } else if (instr_type == InstructionTable.STORE_INSTRUCTION) {
                                instr.addBefore("SudokuMetricsTool", "loadStore", 1);
                            }
                        }
                    }
                }
                ci.write(in_filename);
            }
        }
    }

    public static void addBranchInstrumentation(File in_dir) {
        String[] fileList = in_dir.list();

        for (String filename : fileList) {
            if (filename.endsWith(".class")) {
                String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                ClassInfo ci = new ClassInfo(in_filename);
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    InstructionArray instructions = routine.getInstructionArray();
                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        Instruction instr = instructions.elementAt(bb.getEndAddress());
                        short instr_type = InstructionTable.InstructionTypeTable[instr.getOpcode()];
                        if (instr_type == InstructionTable.CONDITIONAL_INSTRUCTION) {
                            instr.addBefore("SudokuMetricsTool", "updateBranch", "null");
                        }
                    }
                }
                ci.write(in_filename);
            }
        }
    }

    public static void addSolveCallback(File in_dir) {
        String[] fileList = in_dir.list();

        for (String filename : fileList) {
            if (filename.endsWith(".class")) {
                String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                ClassInfo ci = new ClassInfo(in_filename);
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    if (routine.getMethodName().equals("solveSudoku")) {
                        routine.addAfter("SudokuMetricsTool", "saveStats", "null");
                    }
                }
                ci.write(in_filename);
            }
        }
    }


    public static void saveStats(String foo) {
        SolverArgumentParser parser = WebServer.getCurrentThreadBoard();
        writeToFile(getCurrentStats(), parser);
        //Thread is finished after SolveSudoku
        stats.remove(getCurrentThreadName());
    }

    public static void writeToFile(Stats stats, SolverArgumentParser parser) {
        JSONObject object = stats.toJSON();
        object.put("Board", parser.toJSON());
        String outputDir = "./out";
        String path = outputDir + System.getProperty("file.separator") + UUID.randomUUID().toString() + ".json";
        try (PrintWriter out = new PrintWriter(path)) {
            out.println(object.toString());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void instr(int incr) {
        Stats stats = getCurrentStats();
        stats.incrInstructionCount(incr);
        stats.incrBasicBlockCount();
    }

    public static void method(int foo) {
        getCurrentStats().incrMethodCount();
    }

    public static void alloc(int type) {
        switch (type) {
            case InstructionTable.NEW:
                getCurrentStats().incrNewCount();
                break;
            case InstructionTable.newarray:
                getCurrentStats().incrNewArrayCount();
                break;
            case InstructionTable.anewarray:
                getCurrentStats().incrANewArrayCount();
                break;
            case InstructionTable.multianewarray:
                getCurrentStats().incrMultiANewArrayCount();
                break;
        }
    }

    public static void loadStoreField(int type) {
        Stats stats = getCurrentStats();
        if (type == 0) {
            stats.incrFieldLoadCount();
        } else {
            stats.incrFieldStoreCount();
        }
    }

    public static void loadStore(int type) {
        Stats stats = getCurrentStats();
        if (type == 0) {
            stats.incrLoadCount();
        } else {
            stats.incrStoreCount();
        }
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
                addInstructionInstrumentation(in_dir);
                addLoadStoreInstrumentation(in_dir);
                addAlocationInstrumentation(in_dir);
                addBranchInstrumentation(in_dir);
                addSolveCallback(in_dir);
            } else {
                printUsage();
            }
        } catch (NullPointerException e) {
            printUsage();
        }
    }
}

