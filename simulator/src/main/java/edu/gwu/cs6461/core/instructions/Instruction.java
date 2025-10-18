package edu.gwu.cs6461.core.instructions;

import edu.gwu.cs6461.core.InstructionDecoder.Decoded;
import edu.gwu.cs6461.core.Memory;
import edu.gwu.cs6461.core.Registers;
import edu.gwu.cs6461.core.util.MachineFaultException;

public interface Instruction {
    void execute(Decoded d, Registers regs, Memory mem) throws MachineFaultException;
}
