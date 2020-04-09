import BIT.highBIT.*;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import pt.ulisboa.tecnico.cnv.server.WebServer;

public class SudokuMetricsTool {
    private static ConcurrentHashMap<String, Stats> stats = new ConcurrentHashMap<>();

    public static String getCurrentThreadName() {
        return String.valueOf(Thread.currentThread().getId());
    }

    private static Stats getCurrentStats() {
        String name = getCurrentThreadName();
        stats.putIfAbsent(name, new Stats());
        return stats.get(name);
    }

    private static Stats getStatsForThread(String name) {
        stats.putIfAbsent(name, new Stats());
        return stats.get(name);
    }

    public static void printUsage() {
        System.out.println("Syntax: java StatisticsTool -stat_type in_path [out_path]");
        System.out.println("        where stat_type can be:");
        System.out.println("        static:     static properties");
        System.out.println("        dynamic:    dynamic properties");
        System.out.println("        alloc:      memory allocation instructions");
        System.out.println("        load_store: loads and stores (both field and regular)");
        System.out.println("        branch:     gathers branch outcome statistics");
        System.out.println("        all:        gathers all dynamic properties");
        System.out.println();
        System.out.println("        in_path:  directory from which the class files are read");
        System.out.println("        out_path: directory to which the class files are written");
        System.out.println("        Both in_path and out_path are required unless stat_type is static");
        System.out.println("        in which case only in_path is required");
        System.exit(-1);
    }

    public static void doStatic(File in_dir) {
        String filelist[] = in_dir.list();
        int method_count = 0;
        int bb_count = 0;
        int instr_count = 0;
        int class_count = 0;

        for (int i = 0; i < filelist.length; i++) {
            String filename = filelist[i];
            if (filename.endsWith(".class")) {
                class_count++;
                String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                ClassInfo ci = new ClassInfo(in_filename);
                Vector routines = ci.getRoutines();
                method_count += routines.size();

                for (Enumeration e = routines.elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    BasicBlockArray bba = routine.getBasicBlocks();
                    bb_count += bba.size();
                    InstructionArray ia = routine.getInstructionArray();
                    instr_count += ia.size();
                }
            }
        }

        System.out.println("Static information summary:");
        System.out.println("Number of class files:  " + class_count);
        System.out.println("Number of methods:      " + method_count);
        System.out.println("Number of basic blocks: " + bb_count);
        System.out.println("Number of instructions: " + instr_count);

        if (class_count == 0 || method_count == 0) {
            return;
        }

        float instr_per_bb = (float) instr_count / (float) bb_count;
        float instr_per_method = (float) instr_count / (float) method_count;
        float instr_per_class = (float) instr_count / (float) class_count;
        float bb_per_method = (float) bb_count / (float) method_count;
        float bb_per_class = (float) bb_count / (float) class_count;
        float method_per_class = (float) method_count / (float) class_count;

        System.out.println("Average number of instructions per basic block: " + instr_per_bb);
        System.out.println("Average number of instructions per method:      " + instr_per_method);
        System.out.println("Average number of instructions per class:       " + instr_per_class);
        System.out.println("Average number of basic blocks per method:      " + bb_per_method);
        System.out.println("Average number of basic blocks per class:       " + bb_per_class);
        System.out.println("Average number of methods per class:            " + method_per_class);
    }

    public static void doDynamic(File in_dir, File out_dir) {
        String filelist[] = in_dir.list();

        for (int i = 0; i < filelist.length; i++) {
            String filename = filelist[i];
            if (filename.endsWith(".class")) {
                String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                ClassInfo ci = new ClassInfo(in_filename);
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    routine.addBefore("StatisticsTool", "dynMethodCount", new Integer(1));

                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        bb.addBefore("StatisticsTool", "dynInstrCount", new Integer(bb.size()));
                    }
                }
                ci.addAfter("StatisticsTool", "printDynamic", "null");
                ci.write(out_filename);
            }
        }
    }

    public static void printDynamic(String foo) {
        for (String thread : stats.keySet()) {
            printDynamic(foo, thread);
        }
    }

    public static void printDynamic(String foo, String thread) {
        System.out.println(String.format("Dynamic information summary for thread %s:", thread));

        Stats stats = getStatsForThread(thread);
        System.out.println("Number of methods:      " + stats.getDyn_method_count());
        System.out.println("Number of basic blocks: " + stats.getDyn_bb_count());
        System.out.println("Number of instructions: " + stats.getDyn_instr_count());

        if (stats.getDyn_method_count() == 0) {
            return;
        }

        float instr_per_bb = (float) stats.getDyn_instr_count() / (float) stats.getDyn_bb_count();
        float instr_per_method = (float) stats.getDyn_instr_count() / (float) stats.getDyn_method_count();
        float bb_per_method = (float) stats.getDyn_bb_count() / (float) stats.getDyn_method_count();

        System.out.println("Average number of instructions per basic block: " + instr_per_bb);
        System.out.println("Average number of instructions per method:      " + instr_per_method);
        System.out.println("Average number of basic blocks per method:      " + bb_per_method);
    }


    public static void dynInstrCount(int incr) {
        Stats stats = getCurrentStats();
        stats.incrDyn_instr_count(incr);
        stats.incrDyn_bb_count(1);
    }

    public static void dynMethodCount(int incr) {
        getCurrentStats().incrDyn_method_count(1);
    }

    public static void doAlloc(File in_dir, File out_dir) {
        String filelist[] = in_dir.list();

        for (int i = 0; i < filelist.length; i++) {
            String filename = filelist[i];
            if (filename.endsWith(".class")) {
                String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
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
                            instr.addBefore("StatisticsTool", "allocCount", new Integer(opcode));
                        }
                    }
                }
                ci.addAfter("StatisticsTool", "printAlloc", "null");
                ci.write(out_filename);
            }
        }
    }

    public static void printAlloc(String s) {
        for (String thread : stats.keySet()) {
            printAlloc(s, thread);
        }
    }

    public static void printAlloc(String s, String thread) {
        System.out.println(String.format("Allocations summary for thread %s:", thread));
        Stats stats = getStatsForThread(thread);
        System.out.println("new:            " + stats.getNewcount());
        System.out.println("newarray:       " + stats.getNewarraycount());
        System.out.println("anewarray:      " + stats.getAnewarraycount());
        System.out.println("multianewarray: " + stats.getMultianewarraycount());
    }

    public static void allocCount(int type) {
        switch (type) {
            case InstructionTable.NEW:
                getCurrentStats().incrNewcount(1);
                break;
            case InstructionTable.newarray:
                getCurrentStats().incrNewarraycount(1);
                break;
            case InstructionTable.anewarray:
                getCurrentStats().incrAnewarraycount(1);
                break;
            case InstructionTable.multianewarray:
                getCurrentStats().incrMultianewarraycount(1);
                break;
        }
    }

    public static void doLoadStore(File in_dir, File out_dir) {
        String filelist[] = in_dir.list();

        for (int i = 0; i < filelist.length; i++) {
            String filename = filelist[i];
            if (filename.endsWith(".class")) {
                String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                ClassInfo ci = new ClassInfo(in_filename);

                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();

                    for (Enumeration instrs = (routine.getInstructionArray()).elements(); instrs.hasMoreElements(); ) {
                        Instruction instr = (Instruction) instrs.nextElement();
                        int opcode = instr.getOpcode();
                        if (opcode == InstructionTable.getfield)
                            instr.addBefore("StatisticsTool", "LSFieldCount", new Integer(0));
                        else if (opcode == InstructionTable.putfield)
                            instr.addBefore("StatisticsTool", "LSFieldCount", new Integer(1));
                        else {
                            short instr_type = InstructionTable.InstructionTypeTable[opcode];
                            if (instr_type == InstructionTable.LOAD_INSTRUCTION) {
                                instr.addBefore("StatisticsTool", "LSCount", new Integer(0));
                            } else if (instr_type == InstructionTable.STORE_INSTRUCTION) {
                                instr.addBefore("StatisticsTool", "LSCount", new Integer(1));
                            }
                        }
                    }
                }
                ci.addAfter("StatisticsTool", "printLoadStore", "null");
                ci.write(out_filename);
            }
        }
    }

    public static void printLoadStore(String s) {
        for (String thread : stats.keySet()) {
            printLoadStore(s, thread);
        }
    }

    public static void printLoadStore(String s, String thread) {
        Stats stats = getStatsForThread(thread);
        System.out.println(String.format("Load Store Summary for thread %s: ", thread));
        System.out.println("Field load:    " + stats.getFieldloadcount());
        System.out.println("Field store:   " + stats.getFieldstorecount());
        System.out.println("Regular load:  " + stats.getLoadcount());
        System.out.println("Regular store: " + stats.getStorecount());
    }

    public static void LSFieldCount(int type) {
        Stats stats = getCurrentStats();
        if (type == 0) {
            stats.incrFieldloadcount(1);
        } else {
            stats.incrFieldstorecount(1);
        }
    }

    public static void LSCount(int type) {
        Stats stats = getCurrentStats();
        if (type == 0) {
            stats.incrLoadcount(1);
        } else {
            stats.incrStorecount(1);
        }
    }

    public static void doBranch(File in_dir, File out_dir) {
        String filelist[] = in_dir.list();
        int k = 0;
        int total = 0;

        for (int i = 0; i < filelist.length; i++) {
            String filename = filelist[i];
            if (filename.endsWith(".class")) {
                String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                ClassInfo ci = new ClassInfo(in_filename);

                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    InstructionArray instructions = routine.getInstructionArray();
                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        Instruction instr = (Instruction) instructions.elementAt(bb.getEndAddress());
                        short instr_type = InstructionTable.InstructionTypeTable[instr.getOpcode()];
                        if (instr_type == InstructionTable.CONDITIONAL_INSTRUCTION) {
                            total++;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < filelist.length; i++) {
            String filename = filelist[i];
            if (filename.endsWith(".class")) {
                String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
                ClassInfo ci = new ClassInfo(in_filename);

                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    routine.addBefore("StatisticsTool", "setBranchMethodName", routine.getMethodName());
                    InstructionArray instructions = routine.getInstructionArray();
                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        Instruction instr = (Instruction) instructions.elementAt(bb.getEndAddress());
                        short instr_type = InstructionTable.InstructionTypeTable[instr.getOpcode()];
                        if (instr_type == InstructionTable.CONDITIONAL_INSTRUCTION) {
                            instr.addBefore("StatisticsTool", "setBranchClassName", ci.getClassName());
                            instr.addBefore("StatisticsTool", "setBranchMethodName", routine.getMethodName());
                            instr.addBefore("StatisticsTool", "setBranchPC", new Integer(instr.getOffset()));
                            instr.addBefore("StatisticsTool", "updateBranchNumber", new Integer(k));
                            instr.addBefore("StatisticsTool", "updateBranchOutcome", "BranchOutcome");
                            k++;
                        }
                    }
                }
                ci.addBefore("StatisticsTool", "setBranchClassName", ci.getClassName());
                ci.addBefore("StatisticsTool", "branchInit", new Integer(total));
                ci.addAfter("StatisticsTool", "printBranch", "null");
                ci.write(out_filename);
            }
        }
    }

    public static void setBranchClassName(String name) {
        getCurrentStats().setBranch_class_name(name);
    }

    public static void setBranchMethodName(String name) {
        getCurrentStats().setBranch_method_name(name);
    }

    public static void setBranchPC(int pc) {
        getCurrentStats().setBranch_pc(pc);
    }

    public static void branchInit(int n) {
        Stats.setBranchCount(n);

    }

    public static void updateBranchNumber(int n) {
        Stats stats = getCurrentStats();
        stats.setBranch_number(n);
        if (stats.getBranch_info()[stats.getBranch_number()] == null) {
            stats.getBranch_info()[stats.getBranch_number()] = new StatisticsBranch(stats.getBranch_class_name(), stats.getBranch_method_name(), stats.getBranch_pc());
        }
    }

    public static void updateBranchOutcome(int br_outcome) {
        Stats stats = getCurrentStats();
        if (br_outcome == 0) {
            stats.getBranch_info()[stats.getBranch_number()].incrNotTaken();
        } else {
            stats.getBranch_info()[stats.getBranch_number()].incrTaken();
        }
    }

    public static void printBranch(String foo) {
        System.out.println("Branch summary: ");
        System.out.println("CLASS NAME" + '\t' + "METHOD" + '\t' + "PC" + '\t' + "TAKEN" + '\t' + "NOT_TAKEN");

        for (String thread : stats.keySet()) {
            printBranch(foo, thread);
        }
    }

    public static void printBranch(String foo, String thread) {
        System.out.println(String.format("Branch summary for thread %s: ", thread));
        System.out.println("CLASS NAME" + '\t' + "METHOD" + '\t' + "PC" + '\t' + "TAKEN" + '\t' + "NOT_TAKEN");
        Stats stats = getStatsForThread(thread);
        for (int i = 0; i < stats.getBranch_info().length; i++) {
            if (stats.getBranch_info()[i] != null) {
                stats.getBranch_info()[i].print();
            }
        }
    }


    public static void main(String argv[]) {
        if (argv.length < 2 || !argv[0].startsWith("-")) {
            printUsage();
        }

        if (argv[0].equals("-static")) {
            if (argv.length != 2) {
                printUsage();
            }

            try {
                File in_dir = new File(argv[1]);

                if (in_dir.isDirectory()) {
                    doStatic(in_dir);
                } else {
                    printUsage();
                }
            } catch (NullPointerException e) {
                printUsage();
            }
        } else if (argv[0].equals("-dynamic")) {
            if (argv.length != 3) {
                printUsage();
            }

            try {
                File in_dir = new File(argv[1]);
                File out_dir = new File(argv[2]);

                if (in_dir.isDirectory() && out_dir.isDirectory()) {
                    doDynamic(in_dir, out_dir);
                } else {
                    printUsage();
                }
            } catch (NullPointerException e) {
                printUsage();
            }
        } else if (argv[0].equals("-alloc")) {
            if (argv.length != 3) {
                printUsage();
            }

            try {
                File in_dir = new File(argv[1]);
                File out_dir = new File(argv[2]);

                if (in_dir.isDirectory() && out_dir.isDirectory()) {
                    doAlloc(in_dir, out_dir);
                } else {
                    printUsage();
                }
            } catch (NullPointerException e) {
                printUsage();
            }
        } else if (argv[0].equals("-load_store")) {
            if (argv.length != 3) {
                printUsage();
            }

            try {
                File in_dir = new File(argv[1]);
                File out_dir = new File(argv[2]);

                if (in_dir.isDirectory() && out_dir.isDirectory()) {
                    doLoadStore(in_dir, out_dir);
                } else {
                    printUsage();
                }
            } catch (NullPointerException e) {
                printUsage();
            }
        } else if (argv[0].equals("-branch")) {
            if (argv.length != 3) {
                printUsage();
            }

            try {
                File in_dir = new File(argv[1]);
                File out_dir = new File(argv[2]);

                if (in_dir.isDirectory() && out_dir.isDirectory()) {
                    doBranch(in_dir, out_dir);
                } else {
                    printUsage();
                }
            } catch (NullPointerException e) {
                printUsage();
            }
        } else if (argv[0].equals("-all")) {
            if (argv.length != 3) {
                printUsage();
            }

            try {
                File in_dir = new File(argv[1]);
                File out_dir = new File(argv[2]);

                if (in_dir.isDirectory() && out_dir.isDirectory()) {
                    doDynamic(in_dir, out_dir);
                    doLoadStore(out_dir, out_dir);
                    doAlloc(out_dir, out_dir);
                    doBranch(out_dir, out_dir);
                } else {
                    printUsage();
                }
            } catch (NullPointerException e) {
                printUsage();
            }
        }

    }
}