package metrics.tools;

import BIT.highBIT.*;

import java.io.File;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

public class SudokuMetricsTool {

    private static final ConcurrentHashMap<Long, RequestStats> stats = new ConcurrentHashMap<>();

    public static long getCurrentThreadId() {
        return Thread.currentThread().getId();
    }

    public static RequestStats getCurrentStats() {
        //no thread tries to access the same key at the same time
        Long id = getCurrentThreadId();
        RequestStats stat = stats.get(id);
        if (stat == null) {
            stat = new RequestStats();
            stats.put(id, stat);
        }
        return stat;
    }

    public static RequestStats getAndRemoveCurrentStats() {
        return stats.remove(getCurrentThreadId());
    }

    public static void printUsage() {
        System.out.println("Syntax: java SudokuMetricsBFS in_dir ");
        System.out.println();
        System.out.println("        in_dir:  directory from which the class files are read and to which they are written");
        System.exit(-1);
    }

    public static void doAlloc(InstructionArray instructions) {
        for (Enumeration instrs = instructions.elements(); instrs.hasMoreElements(); ) {
            Instruction instr = (Instruction) instrs.nextElement();
            int opcode = instr.getOpcode();
            if (opcode == InstructionTable.NEW) {
                instr.addBefore("metrics/tools/SudokuMetricsTool", "allocNew", "null");
            }
        }
    }

    public static void doRoutine(Routine routine) {
        routine.addBefore("metrics/tools/SudokuMetricsTool", "method", 1);
    }


    public static void addInstrumentation(File in_file) {
        String in_filename = in_file.getAbsolutePath();
        ClassInfo ci = new ClassInfo(in_filename);
        for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
            Routine routine = (Routine) e.nextElement();
            InstructionArray instructions = routine.getInstructionArray();
            doRoutine(routine);
            doAlloc(instructions);
        }
        ci.write(in_filename);
    }

    public static void addInstrumentationToDir(File in_dir) {
        String filelist[] = in_dir.list();
        for (String filename : filelist) {
            if (filename.endsWith(".class")) {
                addInstrumentation(new File(filename));
            }
        }
    }


    public static void method(int foo) {
        getCurrentStats().incrMethodCount();
    }

    public static void allocNew(int foo) {
        getCurrentStats().incrNewCount();
    }


    public static void main(String[] argv) {
        if (argv.length != 1) {
            printUsage();
        }
        try {
            File in_dir = new File(argv[0]);
            if (in_dir.exists() && in_dir.isDirectory()) {
                addInstrumentationToDir(in_dir);
            } else {
                printUsage();
            }
        } catch (NullPointerException e) {
            printUsage();
        }
    }
}
