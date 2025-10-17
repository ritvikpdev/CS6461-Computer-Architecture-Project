package edu.gwu.cs6461.core;

import java.util.Arrays;

public class Memory {
    private final int[] mem;

    public Memory(int words){ this.mem = new int[words]; reset(); }
    public int size(){ return mem.length; }
    public void reset(){ Arrays.fill(mem, 0); }

    public int read(int addr){
        bounds(addr);
        return mem[addr] & 0xFFFF;
    }
    public void write(int addr, int value){
        bounds(addr);
        mem[addr] = value & 0xFFFF;
    }
    private void bounds(int a){
        if(a < 0 || a >= mem.length) throw new IndexOutOfBoundsException(""Memory OOB: ""+a);
    }
}
