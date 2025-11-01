import javax.swing.SwingUtilities;

public class CPU {
    private final Memory mem;
    private final Cache cache;

    // General purpose and index registers
    private final int[] GPR = new int[4];
    private final int[] IXR = new int[4]; // 1..3 used

    private int PC, MAR, MBR, IR;
    private boolean halted = false;

    private Runnable onStepUpdate;

    public CPU(Memory mem, Cache cache) {
        this.mem = mem;
        this.cache = cache;
    }

    public void setOnStepUpdate(Runnable cb) { this.onStepUpdate = cb; }

    public void step() {
        if (halted) return;

        // FETCH (via cache)
        MAR = PC;
        MBR = cache.read(MAR);
        IR  = MBR;
        PC = (PC + 1) & 0xFFFF;

        System.out.printf("FETCH: PC=%04o MAR=%04o MBR=%06o IR=%06o\n", PC, MAR, MBR, IR);

        // EXECUTE
        decodeExecute(IR);

        // GUI refresh
        if (onStepUpdate != null) SwingUtilities.invokeLater(onStepUpdate);
    }

    public void run() {
        while (!halted) {
            step();
            try { Thread.sleep(120); } catch (InterruptedException ignore) {}
        }
    }

    // ========= DECODE/EXECUTE (octal layout that matched your assembler) =========
    private void decodeExecute(int inst) {
        // | opcode (6) | r (2) | ix (2) | i (1) | addr (low bits) |
        int opcode = (inst >> 9) & 077;
        int r      = (inst >> 7) & 03;
        int ix     = (inst >> 5) & 03;
        int i      = (inst >> 4) & 01;
        int addr   =  inst & 017; // matches your provided assembler

        switch (opcode) {
            case 040: LDX(ix, addr); break;
            case 043: STX(ix, addr); break;
            case 045: LDR(r, ix, i, addr); break;
            case 046: STR(r, ix, i, addr); break;
            case 050: AMR(r, ix, i, addr); break;
            case 053: SMR(r, ix, i, addr); break;
            case 054: AIR(r, addr); break;
            case 056: SIR(r, addr); break;
            case 061: LDA(r, ix, i, addr); break;
            case 063: LDA(r, ix, i, addr); break;  // assembler variant
            case 065: JCC(addr); break;            // unconditional for Part 1/2
            case 066: JMA(addr); break;
            case 000: halt(); System.out.println("HALT executed."); break;
            default:
                System.out.printf("Unknown opcode %02o at %04o\n", opcode, MAR);
        }
    }

    // ================= Instructions (use cache) =============

    private void LDX(int ix, int addr) {
        if (ix == 0) return;
        IXR[ix] = cache.read(addr);
        System.out.printf("LDX: IX%d ← M[%06o]=%06o\n", ix, addr, IXR[ix]);
    }

    private void STX(int ix, int addr) {
        if (ix == 0) return;
        cache.write(addr, IXR[ix]);
        System.out.printf("STX: M[%06o]←IX%d(%06o)\n", addr, ix, IXR[ix]);
    }

    private void LDR(int r, int ix, int i, int addr) {
        int EA = calcEA(ix, i, addr);
        GPR[r] = cache.read(EA);
        System.out.printf("LDR: R%d←M[%06o]=%06o\n", r, EA, GPR[r]);
    }

    private void STR(int r, int ix, int i, int addr) {
        int EA = calcEA(ix, i, addr);
        cache.write(EA, GPR[r]);
        System.out.printf("STR: M[%06o]←R%d(%06o)\n", EA, r, GPR[r]);
    }

    private void AMR(int r, int ix, int i, int addr) {
        int EA = calcEA(ix, i, addr);
        GPR[r] = (GPR[r] + cache.read(EA)) & 0xFFFF;
        System.out.printf("AMR: R%d←R%d+M[%06o]=%06o\n", r, r, EA, GPR[r]);
    }

    private void SMR(int r, int ix, int i, int addr) {
        int EA = calcEA(ix, i, addr);
        GPR[r] = (GPR[r] - cache.read(EA)) & 0xFFFF;
        System.out.printf("SMR: R%d←R%d-M[%06o]=%06o\n", r, r, EA, GPR[r]);
    }

    private void AIR(int r, int imm) {
        GPR[r] = (GPR[r] + (imm & 0xFFFF)) & 0xFFFF;
        System.out.printf("AIR: R%d←R%d+%06o=%06o\n", r, r, imm, GPR[r]);
    }

    private void SIR(int r, int imm) {
        GPR[r] = (GPR[r] - (imm & 0xFFFF)) & 0xFFFF;
        System.out.printf("SIR: R%d←R%d-%06o=%06o\n", r, r, imm, GPR[r]);
    }

    private void LDA(int r, int ix, int i, int addr) {
        int EA = calcEA(ix, i, addr);
        GPR[r] = EA & 0xFFFF;
        System.out.printf("LDA: R%d←EA(%06o)\n", r, EA);
    }

    private void JCC(int addr) {
        PC = addr & 0xFFFF;
        System.out.printf("JCC: PC←%06o\n", PC);
    }

    private void JMA(int addr) {
        PC = addr & 0xFFFF;
        System.out.printf("JMA: PC←%06o\n", PC);
    }

    // ================= Effective Address via cache (for indirect) ================
    private int calcEA(int ix, int i, int addr) {
        int EA = addr & 0xFFFF;
        if (ix > 0) EA = (EA + IXR[ix]) & 0xFFFF;
        if (i == 1) EA = cache.read(EA) & 0xFFFF;
        return EA;
    }

    // ================= Accessors =================
    public void halt() { halted = true; }
    public boolean isHalted() { return halted; }

    public int getGPR(int n) { return GPR[n] & 0xFFFF; }
    public void setGPR(int n, int val) { GPR[n] = val & 0xFFFF; }

    public int getIXR(int n) { return IXR[n] & 0xFFFF; }
    public void setIXR(int n, int val) { IXR[n] = val & 0xFFFF; }

    public int getPC() { return PC & 0xFFFF; }
    public void setPC(int val) { PC = val & 0xFFFF; }

    public int getMAR() { return MAR & 0xFFFF; }
    public int getMBR() { return MBR & 0xFFFF; }
    public int getIR()  { return IR  & 0xFFFF; }

    public Cache getCache() { return cache; }
}
