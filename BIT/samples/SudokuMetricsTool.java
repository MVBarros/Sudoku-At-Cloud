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
        System.out.println("        in_path:  directory from which the class files are read");
        System.exit(-1);
    }

    public static void addInstructionInstrumentation(File in_dir) {
        String[] filelist = in_dir.list();

        for (String filename : filelist) {
            if (filename.endsWith(".class")) {
                String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                ClassInfo ci = new ClassInfo(in_filename);
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    routine.addBefore("SudokuMetricsTool", "methodCount", 1);

                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        bb.addBefore("SudokuMetricsTool", "instrCount", bb.size());
                    }
                }
                ci.write(in_filename);
            }
        }
    }

    public static void addAlocationInstrumentation(File in_dir) {
        String[] filelist = in_dir.list();

        for (String filename : filelist) {
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
                            instr.addBefore("SudokuMetricsTool", "allocCount", opcode);
                        }
                    }
                }
                ci.write(in_filename);
            }
        }
    }

    public static void addLoadStoreInstrumentation(File in_dir) {
        String[] filelist = in_dir.list();

        for (String filename : filelist) {
            if (filename.endsWith(".class")) {
                String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                ClassInfo ci = new ClassInfo(in_filename);

                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();

                    for (Enumeration instrs = (routine.getInstructionArray()).elements(); instrs.hasMoreElements(); ) {
                        Instruction instr = (Instruction) instrs.nextElement();
                        int opcode = instr.getOpcode();
                        if (opcode == InstructionTable.getfield)
                            instr.addBefore("SudokuMetricsTool", "LSFieldCount", 0);
                        else if (opcode == InstructionTable.putfield)
                            instr.addBefore("SudokuMetricsTool", "LSFieldCount", 1);
                        else {
                            short instr_type = InstructionTable.InstructionTypeTable[opcode];
                            if (instr_type == InstructionTable.LOAD_INSTRUCTION) {
                                instr.addBefore("SudokuMetricsTool", "LSCount", 0);
                            } else if (instr_type == InstructionTable.STORE_INSTRUCTION) {
                                instr.addBefore("SudokuMetricsTool", "LSCount", 1);
                            }
                        }
                    }
                }
                ci.write(in_filename);
            }
        }
    }

    public static void addBranchInstrumentation(File in_dir) {
        String[] filelist = in_dir.list();
        int k = 0;
        int total = 0;

        for (String filename : filelist) {
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
                            total++;
                        }
                    }
                }
            }
        }

        for (String filename : filelist) {
            if (filename.endsWith(".class")) {
                String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                ClassInfo ci = new ClassInfo(in_filename);

                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    routine.addBefore("SudokuMetricsTool", "setBranchMethodName", routine.getMethodName());
                    InstructionArray instructions = routine.getInstructionArray();
                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        Instruction instr = (Instruction) instructions.elementAt(bb.getEndAddress());
                        short instr_type = InstructionTable.InstructionTypeTable[instr.getOpcode()];
                        if (instr_type == InstructionTable.CONDITIONAL_INSTRUCTION) {
                            instr.addBefore("SudokuMetricsTool", "setBranchClassName", ci.getClassName());
                            instr.addBefore("SudokuMetricsTool", "setBranchMethodName", routine.getMethodName());
                            instr.addBefore("SudokuMetricsTool", "setBranchPC", instr.getOffset());
                            instr.addBefore("SudokuMetricsTool", "updateBranchNumber", k);
                            instr.addBefore("SudokuMetricsTool", "updateBranchOutcome", "BranchOutcome");
                            k++;
                        }
                    }
                    if (routine.getMethodName().equals("solveSudoku")) {
                        routine.addAfter("SudokuMetricsTool", "saveStats", "null");
                    }
                }
                //Before anything set the number of branches that exist
                ci.addBefore("SudokuMetricsTool", "branchInit", total);
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

    public static void instrCount(int incr) {
        Stats stats = getCurrentStats();
        stats.incrInstructionCount(incr);
        stats.incrBasicBlockCount();
    }

    public static void methodCount(int foo) {
        getCurrentStats().incrMethodCount();
    }


    public static void allocCount(int type) {
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

    public static void LSFieldCount(int type) {
        Stats stats = getCurrentStats();
        if (type == 0) {
            stats.incrFieldLoadCount();
        } else {
            stats.incrFieldStoreCount();
        }
    }

    public static void LSCount(int type) {
        Stats stats = getCurrentStats();
        if (type == 0) {
            stats.incrLoadCount();
        } else {
            stats.incrStoreCount();
        }
    }

    public static void setBranchClassName(String name) {
        getCurrentStats().setBranchClassName(name);
    }

    public static void setBranchMethodName(String name) {
        getCurrentStats().setBranchMethodName(name);
    }

    public static void setBranchPC(int pc) {
        getCurrentStats().setBranchPc(pc);
    }

    public static void branchInit(int n) {
        Stats.setBranchCount(n);

    }

    public static void updateBranchNumber(int n) {
        Stats stats = getCurrentStats();
        stats.setBranchNumber(n);
        if (stats.getBranchInfo()[stats.getBranchNumber()] == null) {
            stats.getBranchInfo()[stats.getBranchNumber()] = new StatisticsBranch(stats.getBranchClassName(), stats.getBranchMethodName(), stats.getBranchPc());
        }
    }

    public static void updateBranchOutcome(int br_outcome) {
        Stats stats = getCurrentStats();
        if (br_outcome == 0) {
            stats.getBranchInfo()[stats.getBranchNumber()].incrNotTaken();
        } else {
            stats.getBranchInfo()[stats.getBranchNumber()].incrTaken();
        }
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
            } else {
                printUsage();
            }
        } catch (NullPointerException e) {
            printUsage();
        }
    }
}

