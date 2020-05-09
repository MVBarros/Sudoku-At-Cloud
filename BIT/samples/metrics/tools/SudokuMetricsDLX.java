package metrics.tools;

import BIT.highBIT.*;

import java.io.File;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

public class SudokuMetricsDLX {

    private static final ConcurrentHashMap<Long, StatsDLX> stats = new ConcurrentHashMap<>();

    public static long getCurrentThreadId() {
        return Thread.currentThread().getId();
    }

    public static StatsDLX getCurrentStats() {
        //no thread tries to access the same key at the same time
        Long id = getCurrentThreadId();
        StatsDLX stat = stats.get(id);
        if (stat == null) {
            stat = new StatsDLX();
            stats.put(id, stat);
        }
        return stat;
    }

    public static StatsDLX getAndRemoveCurrentStats() {
        return stats.remove(getCurrentThreadId());
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
            if (opcode == InstructionTable.newarray) {
                instr.addBefore("metrics/tools/SudokuMetricsDLX", "allocNewArray", "null");

            }
        }
    }


    public static void addInstrumentation(File in_file) {
        String filename = in_file.getName();
        if (filename.endsWith(".class")) {
            String in_filename = in_file.getAbsolutePath();
            ClassInfo ci = new ClassInfo(in_filename);
            for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                Routine routine = (Routine) e.nextElement();
                InstructionArray instructions = routine.getInstructionArray();
                doAlloc(instructions);
            }
            ci.write(in_filename);
        }
    }


    public static void allocNewArray(String foo) {
        getCurrentStats().incrNewArrayCount();
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

