package edu.gwu.cs6461.core.instructions;

import java.util.HashMap;
import java.util.Map;

public class InstructionRegistry {
    // Update these to match your assembler later
    public static final int OP_LDR = 1, OP_STR = 2, OP_LDA = 3, OP_LDX = 41, OP_STX = 42;

    private final Map<Integer, Instruction> map = new HashMap<>();
    public InstructionRegistry(){
        map.put(OP_LDR, (op,r,ix,i,a,regs,mem)->{ /* TODO */ });
        map.put(OP_STR, (op,r,ix,i,a,regs,mem)->{ /* TODO */ });
        map.put(OP_LDA, (op,r,ix,i,a,regs,mem)->{ /* TODO */ });
        map.put(OP_LDX, (op,r,ix,i,a,regs,mem)->{ /* TODO */ });
        map.put(OP_STX, (op,r,ix,i,a,regs,mem)->{ /* TODO */ });
    }
    public Instruction resolve(int opcode){ return map.get(opcode); }
}
