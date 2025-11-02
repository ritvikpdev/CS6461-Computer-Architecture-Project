package edu.gwu.cs6461.core;

/**
 * Executes arithmetic and logic operations.
 */
public class ArithmeticUnit {
    private final Registers regs;

    public ArithmeticUnit(Registers regs) { this.regs = regs; }

    // ===== Arithmetic on Memory =====
    public void memoryOp(DecodedInstruction d, Memory mem, char op) {
        int ea = getEA(d, mem);
        short val = mem.getValueAt(ea);
        int result = (op == '+') ? regs.getGPR(d.r()) + val : regs.getGPR(d.r()) - val;
        regs.setGPR(d.r(), result);
        updateFlags(result);
    }

    // ===== Arithmetic on Registers =====
    public void registerOp(DecodedInstruction d, CPU cpu) {
        int rx = d.r(), ry = d.ix();
        switch (d.opcode()) {
            case 20 -> { // MLT
                int res = regs.getGPR(rx) * regs.getGPR(ry);
                regs.setGPR(rx, res & 0xFFFF);
                regs.setGPR(rx + 1, (res >> 16) & 0xFFFF);
            }
            case 21 -> { // DVD
                if (regs.getGPR(ry) == 0) { FaultHandler.divZero(cpu); return; }
                regs.setGPR(rx, regs.getGPR(rx) / regs.getGPR(ry));
                regs.setGPR(rx + 1, regs.getGPR(rx) % regs.getGPR(ry));
            }
            case 22 -> regs.setCC((regs.getGPR(rx) == regs.getGPR(ry)) ? 1 : 0); // TRR
            case 23 -> regs.setGPR(rx, regs.getGPR(rx) & regs.getGPR(ry));       // AND
            case 24 -> regs.setGPR(rx, regs.getGPR(rx) | regs.getGPR(ry));       // ORR
            case 25 -> regs.setGPR(rx, ~regs.getGPR(rx));                        // NOT
        }
    }

    private int getEA(DecodedInstruction d, Memory mem) {
        int ea = (d.ix() == 0) ? d.address() : d.address() + regs.getIXR(d.ix());
        if (d.i() == 1) ea = mem.getValueAt(ea);
        return ea & 0xFFF;
    }

    private void updateFlags(int result) {
        int cc = 0;
        if (result < 0) cc |= 0x8;
        if (result == 0) cc |= 0x4;
        if ((result & 0x10000) != 0) cc |= 0x2;
        regs.setCC(cc);
    }
}
