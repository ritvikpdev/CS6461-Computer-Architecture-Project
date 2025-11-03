package edu.gwu.cs6461.core;

/**
 * Holds all processor registers and provides typed getters/setters.
 * (MODIFIED to safely handle index register 0)
 */
public class Registers {
    private final int[] GPR = new int[4];
    private final int[] IXR = new int[3]; // Physical registers are still 1-3
    private int PC, IR, MAR, MBR, CC, MFR;

    public void reset() {
        PC = IR = MAR = MBR = CC = MFR = 0;
        for (int i = 0; i < GPR.length; i++) GPR[i] = 0;
        for (int i = 0; i < IXR.length; i++) IXR[i] = 0;
    }

    // ===== General-Purpose Registers =====
    public int getGPR(int i) { return GPR[i] & 0xFFFF; }
    public void setGPR(int i, int v) { GPR[i] = v & 0xFFFF; }

    // ===== Index Registers =====
    /**
     * Gets value from Index Register i.
     * CRITICAL FIX: Returns 0 if i is 0, which matches the ISA spec.
     */
    public int getIXR(int i) {
        if (i == 0) return 0; // 0 indicates no indexing
        return IXR[i - 1] & 0xFFFF;
    }

    /**
     * Sets value for Index Register i.
     * CRITICAL FIX: Does nothing if i is 0.
     */
    public void setIXR(int i, int v) {
        if (i == 0) return; // Cannot set register 0
        IXR[i - 1] = v & 0xFFFF;
    }

    // ===== Control Registers =====
    public int getPC() { return PC & 0xFFF; }
    public void setPC(int v) { PC = v & 0xFFF; }
    public void incrementPC() { PC = (PC + 1) & 0xFFF; }

    public int getIR() { return IR & 0xFFFF; }
    public void setIR(int v) { IR = v & 0xFFFF; }

    public int getMAR() { return MAR & 0xFFF; }
    public void setMAR(int v) { MAR = v & 0xFFF; }

    public int getMBR() { return MBR & 0xFFFF; }
    public void setMBR(int v) { MBR = v & 0xFFFF; }

    public int getCC() { return CC & 0xF; }
    public void setCC(int v) { CC = v & 0xF; }
    // Helper to set a specific condition code bit
    public void setCCBit(int bit, boolean value) {
        if (value) {
            CC |= (1 << bit);
        } else {
            CC &= ~(1 << bit);
        }
    }

    public int getMFR() { return MFR & 0xF; }
    public void setMFR(int v) { MFR = v & 0xF; }
}