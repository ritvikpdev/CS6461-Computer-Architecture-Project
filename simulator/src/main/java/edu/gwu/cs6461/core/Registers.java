package edu.gwu.cs6461.core;

public class Registers {
    private final int[] R = new int[4];   // R0..R3
    private final int[] X = new int[4];   // X0 unused, X1..X3
    private int PC, MAR, MBR, IR, CC;

    public int getR(int i){ return R[i & 3] & 0xFFFF; }
    public void setR(int i, int v){ R[i & 3] = v & 0xFFFF; }

    public int getX(int i){ return X[i & 3] & 0xFFFF; }
    public void setX(int i, int v){ X[i & 3] = v & 0xFFFF; }

    public int getPC(){ return PC & 0xFFFF; }
    public void setPC(int v){ PC = v & 0xFFFF; }

    public int getMAR(){ return MAR & 0xFFFF; }
    public void setMAR(int v){ MAR = v & 0xFFFF; }

    public int getMBR(){ return MBR & 0xFFFF; }
    public void setMBR(int v){ MBR = v & 0xFFFF; }

    public int getIR(){ return IR & 0xFFFF; }
    public void setIR(int v){ IR = v & 0xFFFF; }

    public int getCC(){ return CC & 0xF; }
    public void setCCBit(int bit, boolean set){
        int m = 1 << (bit & 3);
        CC = set ? (CC | m) : (CC & ~m);
        CC &= 0xF;
    }
}
