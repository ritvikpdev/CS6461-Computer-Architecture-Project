package edu.gwu.cs6461.core.instructions;

import edu.gwu.cs6461.core.InstructionDecoder.Decoded;
import edu.gwu.cs6461.core.Memory;
import edu.gwu.cs6461.core.Registers;

public class HLT implements Instruction {
    @Override
    public void execute(Decoded d, Registers regs, Memory mem) {
        // We signal HALT by setting CC bit 0x8 (use any bit; controller shows as HALTED)
        regs.setCC(regs.getCC() | 0b1000);
    }
    public static boolean isHalted(Registers regs){ return (regs.getCC() & 0b1000) != 0; }
}
