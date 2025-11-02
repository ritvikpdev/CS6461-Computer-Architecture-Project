package edu.gwu.cs6461.core;

/**
 * Decodes a 16-bit instruction into its components.
 */
public class InstructionDecoder {

    public DecodedInstruction decode(int IR) {
        int opcode = (IR >> 10) & 0x3F;
        int r = (IR >> 8) & 0x3;
        int ix = (IR >> 6) & 0x3;
        int i = (IR >> 5) & 0x1;
        int address = IR & 0x1F;
        return new DecodedInstruction(opcode, r, ix, i, address);
    }
}
