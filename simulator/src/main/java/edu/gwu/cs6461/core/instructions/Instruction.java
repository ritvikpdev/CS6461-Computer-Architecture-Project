package edu.gwu.cs6461.core.instructions;

import edu.gwu.cs6461.core.Memory;
import edu.gwu.cs6461.core.Registers;

public interface Instruction {
    void execute(int opcode, int r, int ix, boolean indirect, int addr5,
                 Registers regs, Memory mem);
}
