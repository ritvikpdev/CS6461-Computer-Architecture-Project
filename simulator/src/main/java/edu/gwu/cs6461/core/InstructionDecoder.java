package edu.gwu.cs6461.core;

/**
 * Decodes a 16-bit instruction into its components.
 * (MODIFIED to handle different instruction formats)
 */
public class InstructionDecoder {

    public DecodedInstruction decode(int IR) {
        // Step 1: Always decode the 6-bit opcode first.
        int opcode = (IR >> 10) & 0x3F;

        // Step 2: Decode the rest of the IR based on the instruction format
        // The octal values correspond to the ISA PDF
        switch (opcode) {
            // Register-to-Register Format: Op(6) Rx(2) Ry(2) Ign(6)
            case 070: // 56 (MLT)
            case 071: // 57 (DVD)
            case 072: // 58 (TRR)
            case 073: // 59 (AND)
            case 074: // 60 (ORR)
            case 075: // 61 (NOT)
                int rx = (IR >> 8) & 0x3;
                int ry = (IR >> 6) & 0x3;
                // Store Ry in the 'ix' field of the record for the ALU
                return new DecodedInstruction(opcode, rx, ry, 0, 0);

            // Shift/Rotate Format: Op(6) R(2) A/L(1) L/R(1) Ign(2) Count(4)
            case 031: // 25 (SRC)
            case 032: // 26 (RRC)
                int r_sh = (IR >> 8) & 0x3;
                int al = (IR >> 7) & 0x1;
                int lr = (IR >> 6) & 0x1;
                int count = IR & 0x0F;
                // Pack A/L and L/R into the 'ix' field
                int ix_sh = (al << 1) | lr;
                // Store Count in the 'address' field
                return new DecodedInstruction(opcode, r_sh, ix_sh, 0, count);

            // I/O Format: Op(6) R(2) Ign(3) DevID(5)
            case 061: // 49 (IN)
            case 062: // 50 (OUT)
            case 063: // 51 (CHK)
                int r_io = (IR >> 8) & 0x3;
                int devid = IR & 0x1F;
                return new DecodedInstruction(opcode, r_io, 0, 0, devid);
            
            // Misc Format (TRAP): Op(6) Ign(6) Code(4)
            case 030: // 24 (TRAP)
                int trapCode = IR & 0x0F;
                return new DecodedInstruction(opcode, 0, 0, 0, trapCode);
            
            // Misc Format (RFS): Op(6) Ign(5) Immed(5)
            case 015: // 13 (RFS)
                int rfsImmed = IR & 0x1F;
                return new DecodedInstruction(opcode, 0, 0, 0, rfsImmed);

            // Transfer Format (JMA): Op(6) Ign(2) IX(2) I(1) Addr(5)
            case 013: // 11 (JMA)
                int ix_jma = (IR >> 6) & 0x3;
                int i_jma = (IR >> 5) & 0x1;
                int addr_jma = IR & 0x1F;
                return new DecodedInstruction(opcode, 0, ix_jma, i_jma, addr_jma);
                
            // Standard Format: Op(6) R(2) IX(2) I(1) Addr(5)
            // This covers all other instructions:
            // LDR, STR, LDA, LDX, STX
            // AMR, SMR, AIR, SIR
            // JZ, JNE, JCC, JSR, SOB, JGE
            // HLT
            default:
                int r_std = (IR >> 8) & 0x3;
                int ix_std = (IR >> 6) & 0x3;
                int i_std = (IR >> 5) & 0x1;
                int addr_std = IR & 0x1F;
                return new DecodedInstruction(opcode, r_std, ix_std, i_std, addr_std);
        }
    }
}