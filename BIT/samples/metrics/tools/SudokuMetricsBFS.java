package metrics.tools;

import BIT.highBIT.ClassInfo;
import BIT.highBIT.Routine;

import java.io.File;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

public class SudokuMetricsBFS {

    private static final ConcurrentHashMap<Long, StatsBFS> stats = new ConcurrentHashMap<>();

    public static long getCurrentThreadId() {
        return Thread.currentThread().getId();
    }

    public static StatsBFS getCurrentStats() {
        //no thread tries to access the same key at the same time
        Long id = getCurrentThreadId();
        StatsBFS stat = stats.get(id);
        if (stat == null) {
            stat = new StatsBFS();
            stats.put(id, stat);
        }
        return stat;
    }

    public static StatsBFS getAndRemoveCurrentStats() {
        return stats.remove(getCurrentThreadId());
    }

    public static void printUsage() {
        System.out.println("Syntax: java SudokuMetricsBFS in_file ");
        System.out.println();
        System.out.println("        in_file:  file from which the class files are read and to which they are written");
        System.exit(-1);
    }


    public static void doRoutine(Routine routine) {
        routine.addBefore("metrics/tools/SudokuMetricsBFS", "method", 1);
    }


    public static void addInstrumentation(File in_file) {
        String filename = in_file.getName();

        if (filename.endsWith(".class")) {
            String in_filename = in_file.getAbsolutePath();
            ClassInfo ci = new ClassInfo(in_filename);
            for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                Routine routine = (Routine) e.nextElement();
                doRoutine(routine);
            }
            ci.write(in_filename);
        }
    }


    public static void method(int foo) {
        getCurrentStats().incrMethodCount();
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

