public class BranchInfo {

    public BranchInfo(String className, String methodName, int pc, int number) {
        this.className = className;
        this.methodName = methodName;
        this.pc = pc;
        this.number = number;
    }

    private String className;
    private String methodName;
    private int pc;
    private int number;

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getPc() {
        return pc;
    }

    public int getNumber() {
        return number;
    }
}
