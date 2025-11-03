package edu.gwu.cs6461.core;

import edu.gwu.cs6461.io.ConsoleDevice;
import edu.gwu.cs6461.io.PrinterDevice;

/**
 * Executes decoded instructions and interacts with memory, ALU, and I/O.
 * (MODIFIED with complete logic for Component I and II instructions)
 */
public class Executor {
    private final Memory mem;
    private final Registers regs;
    private final ArithmeticUnit alu;
    private final ConsoleDevice console;
    private final PrinterDevice printer;

    public Executor(Memory mem, Registers regs, ConsoleDevice console, PrinterDevice printer) {
        this.mem = mem;
        this.regs = regs;
        this.console = console;
        this.printer = printer;
        // Pass the ALU a reference to the registers
        this.alu = new ArithmeticUnit(regs); 
    }

    public void execute(DecodedInstruction d, CPU cpu) {
        // Switch on the opcode (using decimal values from ISA PDF)
        switch (d.opcode()) {
            // === Component I: Basic Machine ===
            case 0:  // HLT (Octal 00)
                cpu.halt();
                break;
            case 1:  // LDR (Octal 01)
                ldr(d);
                break;
            case 2:  // STR (Octal 02)
                str(d);
                break;
            case 3:  // LDA (Octal 03)
                lda(d);
                break;
            case 33: // LDX (Octal 41)
                ldx(d);
                break;
            case 34: // STX (Octal 42)
                stx(d);
                break;

            // === Component II: ALU & Memory ===
            case 4:  // AMR (Octal 04)
                alu.memoryOp(d, mem, '+');
                break;
            case 5:  // SMR (Octal 05)
                alu.memoryOp(d, mem, '-');
                break;
            case 6:  // AIR (Octal 06)
                immediate(d, '+');
                break;
            case 7:  // SIR (Octal 07)
                immediate(d, '-');
                break;
                
            // === Component II: ALU Register-to-Register ===
            case 56: // MLT (Octal 70)
            case 57: // DVD (Octal 71)
            case 58: // TRR (Octal 72)
            case 59: // AND (Octal 73)
            case 60: // ORR (Octal 74)
            case 61: // NOT (Octal 75)
                alu.registerOp(d, cpu);
                break;

            // === Component II: Transfer/Jump ===
            case 8:  // JZ (Octal 10)
                jz(d);
                break;
            case 9:  // JNE (Octal 11)
                jne(d);
                break;
            case 10: // JCC (Octal 12)
                jcc(d);
                break;
            case 11: // JMA (Octal 13)
                jma(d);
                break;
            case 12: // JSR (Octal 14)
                jsr(d);
                break;
            case 13: // RFS (Octal 15)
                rfs(d);
                break;
            case 14: // SOB (Octal 16)
                sob(d);
                break;
            case 15: // JGE (Octal 17)
                jge(d);
                break;

            // === Component II: Shift/Rotate ===
            case 25: // SRC (Octal 31)
            case 26: // RRC (Octal 32)
                shiftRotateOp(d);
                break;

            // === Component II: I/O ===
            case 49: // IN (Octal 61)
                console.input(d, regs);
                break;
            case 50: // OUT (Octal 62)
                printer.output(d, regs);
                break;
            
            // === Component III/IV ===
            case 24: // TRAP (Octal 30)
                // TODO: Implement TRAP (Part III)
                break;
            case 51: // CHK (Octal 63)
                // TODO: Implement CHK (Part IV)
                break;

            default:
                FaultHandler.illegalOpcode(cpu, d.opcode());
        }
    }

    // ===== Private Helper Methods for Opcode Logic =====

    // Calculate Effective Address
    private int getEA(DecodedInstruction d) {
        return alu.getEA(d, mem); // Delegate to ALU's helper
    }

    // LDR: Load Register from Memory
    private void ldr(DecodedInstruction d) {
        int ea = getEA(d);
        regs.setGPR(d.r(), mem.getValueAt(ea));
    }

    // STR: Store Register to Memory
    private void str(DecodedInstruction d) {
        int ea = getEA(d);
        mem.setValueAt(ea, (short) regs.getGPR(d.r()));
    }

    // LDA: Load Register with Address
    private void lda(DecodedInstruction d) {
        int ea = getEA(d);
        regs.setGPR(d.r(), ea);
    }

    // LDX: Load Index Register from Memory
    private void ldx(DecodedInstruction d) {
        int ea = getEA(d);
        // The decoder places the index register (x=1..3) in the 'r' field
        regs.setIXR(d.r(), mem.getValueAt(ea));
    }

    // STX: Store Index Register to Memory
    private void stx(DecodedInstruction d) {
        int ea = getEA(d);
        // The decoder places the index register (x=1..3) in the 'r' field
        mem.setValueAt(ea, (short) regs.getIXR(d.r()));
    }
    
    // AIR/SIR: Add/Subtract Immediate
    private void immediate(DecodedInstruction d, char op) {
        // Immediate value is in the 5-bit address field
        int imm = d.address();
        // Get the GPR value as a 16-bit signed short
        short r_val = (short)regs.getGPR(d.r());
        int val; // Use int for calculation to detect overflow

        if (op == '+') { // AIR
            if (imm == 0) val = r_val;
            else val = r_val + imm;
        } else { // SIR
            if (imm == 0) val = r_val;
            else val = r_val - imm;
        }

        // Set Condition Codes
        regs.setCCBit(0, val > Short.MAX_VALUE); // OVERFLOW
        regs.setCCBit(1, val < Short.MIN_VALUE); // UNDERFLOW

        regs.setGPR(d.r(), val);
    
    }

    // ===== Component II: Transfer Instructions =====

    // JZ: Jump If Zero
    private void jz(DecodedInstruction d) {
        if (regs.getGPR(d.r()) == 0) {
            int ea = getEA(d);
            regs.setPC(ea);
        }
    }

    // JNE: Jump If Not Equal
    private void jne(DecodedInstruction d) {
        if (regs.getGPR(d.r()) != 0) {
            int ea = getEA(d);
            regs.setPC(ea);
        }
    }

    // JCC: Jump If Condition Code
    private void jcc(DecodedInstruction d) {
        int ccBit = d.r(); // r field holds the bit to check (0-3)
        // Check if the specified bit is set
        if ((regs.getCC() & (1 << ccBit)) != 0) {
            int ea = getEA(d);
            regs.setPC(ea);
        }
    }

    // JMA: Unconditional Jump To Address
    private void jma(DecodedInstruction d) {
        // 'r' field is ignored
        int ea = getEA(d);
        regs.setPC(ea);
    }

    // JSR: Jump and Save Return Address
    private void jsr(DecodedInstruction d) {
        int ea = getEA(d);
        // "R3 <- PC+1;"
        // The fetch cycle already incremented PC, so getPC() IS PC+1
        regs.setGPR(3, regs.getPC()); 
        // "PC <- EA"
        regs.setPC(ea);
    }

    // RFS: Return From Subroutine
    private void rfs(DecodedInstruction d) {
        // "R0 <- Immed" (Immed is in address field)
        int immed = d.address();
        regs.setGPR(0, immed);
        // "PC <- c(R3)"
        regs.setPC(regs.getGPR(3));
    }

    // SOB: Subtract One and Branch
    private void sob(DecodedInstruction d) {
        int r = d.r();
        // "r <- c(r) - 1"
        int val = regs.getGPR(r) - 1;
        regs.setGPR(r, val);
        // "If c(r) > 0, PC <- EA"
        if (val > 0) {
            int ea = getEA(d);
            regs.setPC(ea);
        }
    }

    // JGE: Jump Greater Than or Equal To
    private void jge(DecodedInstruction d) {
        // We use 16-bit signed shorts, so >= 0 checks the sign bit
        if ((short)regs.getGPR(d.r()) >= 0) {
            int ea = getEA(d);
            regs.setPC(ea);
        }
    }

    // ===== Component II: Shift/Rotate Operations =====

    private void shiftRotateOp(DecodedInstruction d) {
        // Decoder packs fields as:
        // r = d.r()
        // (A/L << 1) | L/R = d.ix()
        // Count = d.address()
        
        int r = d.r();
        int al = (d.ix() >> 1) & 0x1; // 0=Arithmetic, 1=Logical
        int lr = d.ix() & 0x1;        // 0=Right, 1=Left
        int count = d.address();
        
        if (count == 0) return; // No shift
        
        int value = regs.getGPR(r);
        boolean isRotate = (d.opcode() == 26); // 26 = RRC (Octal 032)

        if (lr == 1) { // Left
            if (isRotate) { // RRC, Left
                // Java's << and >>> handle 32 bits, mask to 16 bits
                value = ((value << count) | (value >>> (16 - count))) & 0xFFFF;
            } else { // SRC, Left
                if (al == 1) { // Logical Left
                    value = (value << count) & 0xFFFF;
                } else { // Arithmetic Left
                    // Arithmetic left shift is same as logical left shift
                    // (Sign bit shifts out, 0s shift in)
                    value = (value << count) & 0xFFFF;
                }
            }
        } else { // Right
            if (isRotate) { // RRC, Right
                value = ((value >>> count) | (value << (16 - count))) & 0xFFFF;
            } else { // SRC, Right
                if (al == 1) { // Logical Right
                    value = (value >>> count) & 0xFFFF;
                } else { // Arithmetic Right
                    // Must preserve the sign bit
                    int signBit = value & 0x8000;
                    value = value >>> count;
                    if (signBit != 0) {
                        // Propagate the sign bit 'count' times from the left
                        value |= (~0 << (16 - count));
                    }
                }
            }
        }
        
        // TODO: Set UNDERFLOW/OVERFLOW flags if bits are shifted out
        
        regs.setGPR(r, value);
    }
}