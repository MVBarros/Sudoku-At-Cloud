import BIT.highBIT.*;
import org.json.JSONObject;
import pt.ulisboa.tecnico.cnv.server.WebServer;
import pt.ulisboa.tecnico.cnv.server.MetricUtils;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SudokuMetricsTool {

    private static final ConcurrentHashMap<String, Stats> stats = new ConcurrentHashMap<>();

    private static final Map<BasicBlock, BlockStats> blocks = new HashMap<>();

    private static boolean isIn(BasicBlock block, int addr) {
        return addr >= block.getStartAddress() && addr <= block.getEndAddress();
    }

    private static List<BlockStats> getBlocksForAddress(int addr) {
        List<BlockStats> bbstats = new ArrayList<>();
        for (BasicBlock bb: blocks.keySet()) {
            if (isIn(bb, addr)) {
                bbstats.add(blocks.get(bb));
            }
        }
        return bbstats;
    }

    private static void constructBlocks(Enumeration bblocks) {
        blocks.clear();
        for (Enumeration e = bblocks; e.hasMoreElements(); ) {
            BasicBlock bb = (BasicBlock) e.nextElement();
            blocks.put(bb, new BlockStats());
        }
    }

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
            List<BlockStats> bbstats = getBlocksForAddress(instr.getOffset());
            int opcode = instr.getOpcode();
            switch (opcode) {
                case InstructionTable.NEW:
                    for (BlockStats bbstat : bbstats) {
                        bbstat.incrAllocNew(1);
                    }
                    break;
                case InstructionTable.newarray:
                    for (BlockStats bbstat : bbstats) {
                        bbstat.incrAllocArray(1);
                    }
                    break;
                case InstructionTable.anewarray:
                    for (BlockStats bbstat : bbstats) {
                        bbstat.incrAllocANewArray(1);
                    }
                    break;
                case InstructionTable.multianewarray:
                    for (BlockStats bbstat : bbstats) {
                        bbstat.incrAllocAMultiArray(1);
                    }
                    break;
            }
        }
        for (BasicBlock bb : blocks.keySet()) {
            BlockStats bbstat = blocks.get(bb);
            if (bbstat.getAllocNew() != 0) {
                bb.addBefore("SudokuMetricsTool", "allocNew", bbstat.getAllocNew());
            }
            if (bbstat.getAllocArray() != 0) {
                bb.addBefore("SudokuMetricsTool", "allocNewArray", bbstat.getAllocArray());
            }
            if (bbstat.getAllocANewArray() != 0) {
                bb.addBefore("SudokuMetricsTool", "allocANewArray", bbstat.getAllocANewArray());
            }
            if (bbstat.getAllocAMultiArray() != 0) {
                bb.addBefore("SudokuMetricsTool", "allocMultiNewArray", bbstat.getAllocAMultiArray());
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
                    InstructionArray instructions = routine.getInstructionArray();
                    constructBlocks(routine.getBasicBlocks().elements());
                    doLoadStore(instructions);
                    doBranch(instructions);
                    doInstr(routine, routine.getBasicBlocks().elements());
                    doAlloc(instructions);
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

    public static void allocNew(int count) {
        getCurrentStats().incrNewCount(count);
    }

    public static void allocNewArray(int count) {
        getCurrentStats().incrNewArrayCount(count);
    }

    public static void allocMultiNewArray(int count) {
        getCurrentStats().incrMultiANewArrayCount(count);
    }

    public static void allocANewArray(int count) {
        getCurrentStats().incrANewArrayCount(count);
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

class BlockStats {

    public int getLoads() {
        return loads;
    }

    public void incrLoads(int loads) {
        this.loads += loads;
    }

    public int getStores() {
        return stores;
    }

    public void incrStores(int stores) {
        this.stores += stores;
    }

    public int getLoadFields() {
        return loadFields;
    }

    public void incrLoadFields(int loadFields) {
        this.loadFields += loadFields;
    }

    public int getStoreFields() {
        return storeFields;
    }

    public void incrStoreFields(int storeFields) {
        this.storeFields += storeFields;
    }

    public int getAllocNew() {
        return allocNew;
    }

    public void incrAllocNew(int allocNew) {
        this.allocNew += allocNew;
    }

    public int getAllocArray() {
        return allocArray;
    }

    public void incrAllocArray(int allocArray) {
        this.allocArray += allocArray;
    }

    public int getAllocANewArray() {
        return allocANewArray;
    }

    public void incrAllocANewArray(int allocANewArray) {
        this.allocANewArray += allocANewArray;
    }

    public int getAllocAMultiArray() {
        return allocAMultiArray;
    }

    public void incrAllocAMultiArray(int allocAMultiArray) {
        this.allocAMultiArray += allocAMultiArray;
    }

    private int loads;
    private int stores;
    private int loadFields;
    private int storeFields;
    private int allocNew;
    private int allocArray;
    private int allocANewArray;
    private int allocAMultiArray;
}

