package edu.gwu.cs6461.core;

import java.util.HashMap;
import java.util.Map;

import edu.gwu.cs6461.core.InstructionDecoder.Decoded;
import edu.gwu.cs6461.core.instructions.*;
import edu.gwu.cs6461.core.util.MachineFaultException;
import edu.gwu.cs6461.core.util.FaultType;

public class CPU {
    private final Registers regs;
    private final Memory mem;

    // opcode → Instruction implementation (adjust numbers to your assembler if needed)
    private final Map<Integer, Instruction> table = new HashMap<>();

    public CPU(Registers regs, Memory mem){
        this.regs = regs;
        this.mem  = mem;
        // Default opcodes (example mapping):
        table.put( 1, new LDR());
        table.put( 2, new STR());
        table.put( 3, new LDA());
        table.put(41, new LDX());
        table.put(42, new STX());
        table.put(63, new HLT()); // use 63 as HALT by convention
    }

    public void reset(){
        regs.reset();
        // CC bit 3 (halt) cleared by reset
    }

    public void step() throws MachineFaultException {
        if (HLT.isHalted(regs)) return;

        // FETCH
        int pc = regs.getPC();
        regs.setMAR(pc);
        int instrWord = mem.read(pc);
        regs.setMBR(instrWord);
        regs.setIR(instrWord);
        regs.setPC((pc + 1) & 0xFFFF);

        // DECODE
        Decoded d = InstructionDecoder.decode(instrWord);

        // EXECUTE
        Instruction impl = table.get(d.opcode);
        if (impl == null){
            throw new MachineFaultException(FaultType.ILLEGAL_INSTRUCTION, "Unknown opcode: "+d.opcode);
        }
        impl.execute(d, regs, mem);
    }

    public void run(int maxSteps) throws MachineFaultException {
        int steps = 0;
        while(!HLT.isHalted(regs) && steps < maxSteps){
            step();
            steps++;
        }
    }
}
