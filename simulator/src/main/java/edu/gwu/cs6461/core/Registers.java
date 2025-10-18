package edu.gwu.cs6461.core;

public class Registers {
    // General purpose R0..R3
    private final int[] R = new int[4];
    // Index X1..X3 (slot 0 left unused so we can use ix=0 => "no index")
    private final int[] X = new int[4];

    // Special registers
    private int PC, IR, MAR, MBR, CC;

    // ===== GP =====
    public int getR(int r){ return R[r & 0x3] & 0xFFFF; }
    public void setR(int r, int v){ R[r & 0x3] = v & 0xFFFF; }

    // ===== IX =====
    public int getX(int ix){ return X[ix & 0x3] & 0xFFFF; }
    public void setX(int ix, int v){ X[ix & 0x3] = v & 0xFFFF; }

    // ===== Special =====
    public int getPC(){ return PC & 0xFFFF; }
    public void setPC(int v){ PC = v & 0xFFFF; }

    public int getIR(){ return IR & 0xFFFF; }
    public void setIR(int v){ IR = v & 0xFFFF; }

    public int getMAR(){ return MAR & 0xFFFF; }
    public void setMAR(int v){ MAR = v & 0xFFFF; }

    public int getMBR(){ return MBR & 0xFFFF; }
    public void setMBR(int v){ MBR = v & 0xFFFF; }

    public int getCC(){ return CC & 0xF; }
    public void setCC(int cc){ CC = cc & 0xF; }
    public void setCCBit(int bit, boolean set){
        int m = 1 << (bit & 0x3);
        CC = set ? (CC | m) : (CC & ~m);
        CC &= 0xF;
    }

    public void reset(){
        for(int i=0;i<4;i++) R[i]=0;
        for(int i=0;i<4;i++) X[i]=0;
        PC = IR = MAR = MBR = CC = 0;
    }
}
