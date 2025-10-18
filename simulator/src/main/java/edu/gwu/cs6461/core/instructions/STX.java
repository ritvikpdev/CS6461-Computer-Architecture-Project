package edu.gwu.cs6461.core.instructions;

import edu.gwu.cs6461.core.InstructionDecoder;
import edu.gwu.cs6461.core.InstructionDecoder.Decoded;
import edu.gwu.cs6461.core.Memory;
import edu.gwu.cs6461.core.Registers;

public class STX implements Instruction {
    @Override
    public void execute(Decoded d, Registers regs, Memory mem) throws edu.gwu.cs6461.core.util.MachineFaultException {
        if (d.ix == 0) return;
        int ea = InstructionDecoder.computeEA(d, regs, mem);
        mem.write(ea, regs.getX(d.ix));
    }
}
