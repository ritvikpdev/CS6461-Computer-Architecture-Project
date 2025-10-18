package edu.gwu.cs6461.core;

import java.util.Arrays;
import edu.gwu.cs6461.core.util.MachineFaultException;
import edu.gwu.cs6461.core.util.FaultType;

public class Memory {
    private final int[] mem; // 16-bit words

    public Memory(int words){
        if(words<=0) throw new IllegalArgumentException("memory size > 0");
        mem = new int[words];
        reset();
    }

    public int size(){ return mem.length; }

    public void reset(){ Arrays.fill(mem, 0); }

    public int read(int addr) throws MachineFaultException {
        bounds(addr);
        return mem[addr] & 0xFFFF;
    }

    public void write(int addr, int val) throws MachineFaultException {
        bounds(addr);
        mem[addr] = val & 0xFFFF;
    }

    private void bounds(int a) throws MachineFaultException {
        if(a < 0 || a >= mem.length){
            throw new MachineFaultException(FaultType.ILLEGAL_MEMORY_ADDRESS, "OOB memory access @"+a);
        }
    }
}
