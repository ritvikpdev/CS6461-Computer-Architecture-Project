package edu.gwu.cs6461.core;

public class ArithmeticUnit {
    private final Registers regs;

    public ArithmeticUnit(Registers regs) { this.regs = regs; }

    // ===== Helper: Calculate Effective Address =====
    // This is used by multiple instruction classes
    public int getEA(DecodedInstruction d, Memory mem) {
        // Use the (now-safe) getIXR method
        int ea = d.address() + regs.getIXR(d.ix());
        
        if (d.i() == 1) {
            // Indirection: EA is the address of the address
            ea = mem.getValueAt(ea);
        }
        return ea & 0xFFF; // Memory addresses are 12 bits
    }

    // ===== Arithmetic on Memory (AMR, SMR) =====
    public void memoryOp(DecodedInstruction d, Memory mem, char op) {
        int ea = getEA(d, mem);
        
        // Treat values as 16-bit signed shorts
        short r_val = (short)regs.getGPR(d.r());
        short mem_val = mem.getValueAt(ea);

        // Use int for calculation to detect overflow/underflow
        int result = (op == '+') ? r_val + mem_val : r_val - mem_val;
        
        // Set Condition Codes
        regs.setCCBit(0, result > Short.MAX_VALUE); // OVERFLOW
        regs.setCCBit(1, result < Short.MIN_VALUE); // UNDERFLOW
        
        regs.setGPR(d.r(), result);
    }
   // ===== Arithmetic on Registers (MLT, DVD, TRR, AND, ORR, NOT) =====
   public void registerOp(DecodedInstruction d, CPU cpu) {
        int rx = d.r(), ry = d.ix(); 
        
        switch (d.opcode()) {
            case 56: { // MLT (Octal 70)
                if ((rx != 0 && rx != 2) || (ry != 0 && ry != 2)) {
                    FaultHandler.illegalOpcode(cpu, d.opcode()); 
                    return;
                }
                
                // Treat operands as 16-bit signed shorts
                short rx_val = (short)regs.getGPR(rx);
                short ry_val = (short)regs.getGPR(ry);

                // Result can be up to 32 bits
                long res = (long)rx_val * (long)ry_val;
                
                // Check if result "overflows" a 16-bit signed value
                boolean overflow = (res > Short.MAX_VALUE || res < Short.MIN_VALUE);
                regs.setCCBit(0, overflow);
                // Underflow is not typically set for multiplication
                regs.setCCBit(1, false); 
                
                // ISA: rx contains high bits, rx+1 contains low bits
                regs.setGPR(rx, (int)((res >> 16) & 0xFFFF)); // High bits
                regs.setGPR(rx + 1, (int)(res & 0xFFFF));  // Low bits
                break;
            }
            case 57: { // DVD (Octal 71)
                if ((rx != 0 && rx != 2) || (ry != 0 && ry != 2)) {
                    FaultHandler.illegalOpcode(cpu, d.opcode()); 
                    return;
                }

                // Treat operands as 16-bit signed shorts
                short val_rx = (short)regs.getGPR(rx);
                short val_ry = (short)regs.getGPR(ry);

                if (val_ry == 0) { 
                    regs.setCCBit(2, true); // Set DIVZERO flag
                    FaultHandler.divZero(cpu); 
                    return; 
                }
                
                regs.setGPR(rx, val_rx / val_ry);      // Quotient
                regs.setGPR(rx + 1, val_rx % val_ry);  // Remainder
                break;
            }
            case 58: { // TRR (Octal 72)
                // Note: getGPR returns 0-65535, so this is a 16-bit bitwise comparison
                boolean isEqual = (regs.getGPR(rx) == regs.getGPR(ry));
                regs.setCCBit(3, isEqual); // Set EQUALORNOT bit
                break;
            }
                
            case 59: // AND (Octal 73)
                regs.setGPR(rx, regs.getGPR(rx) & regs.getGPR(ry));
                break;
            case 60: // ORR (Octal 74)
                regs.setGPR(rx, regs.getGPR(rx) | regs.getGPR(ry));
                break;
            case 61: // NOT (Octal 75)
                regs.setGPR(rx, ~regs.getGPR(rx));
                break;
        }
    }
}