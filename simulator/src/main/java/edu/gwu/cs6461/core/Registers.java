package edu.gwu.cs6461.core;

/**
 * Holds all processor registers and provides typed getters/setters.
 */
public class Registers {
    private final int[] GPR = new int[4];
    private final int[] IXR = new int[3];
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
    public int getIXR(int i) { return IXR[i - 1] & 0xFFFF; }
    public void setIXR(int i, int v) { IXR[i - 1] = v & 0xFFFF; }

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

    public int getMFR() { return MFR & 0xF; }
    public void setMFR(int v) { MFR = v & 0xF; }
}
