package edu.gwu.cs6461.assembler;

import java.io.*;
import java.util.*;

public class PassTwo {
    // Use the new, category-aware instruction parser
    private InstructionParser parser = new InstructionParser();

    public void secondPass(List<String[]> program, SymbolTable symTable, String listingFile, String loadFile) throws Exception {
        BufferedWriter bwListing = new BufferedWriter(new FileWriter(listingFile));
        BufferedWriter bwLoad    = new BufferedWriter(new FileWriter(loadFile));

        for (String[] entry : program) {
            int addr = Integer.parseInt(entry[0]);
            String type = entry[1];
            String line = entry[2];
            String raw = entry[3];
            String label = entry[4]; // optional

            // Build source line with label if present
            String sourceLine = (label != null ? label + ": " : "      ") + raw.trim();

            if (type.equals("LOC")) {
                // Write to listing file only
                bwListing.write(String.format("%-6s %-6s   %s", " ", " ", sourceLine));
                bwListing.newLine();
                continue; // no machine code generated
            }

            if (type.equals("DATA")) {
                // Support multiple comma-separated DATA values on one line
                String payload = line.substring(line.indexOf(' ') + 1).trim();
                String[] values = payload.replaceAll("\\s*,\\s*", ",").split(",");

                int curAddr = addr;
                for (String v : values) {
                    int value;
                    if (v.matches("\\d+")) {
                        value = Integer.parseInt(v);
                    } else if (symTable.contains(v)) {
                        value = symTable.get(v);
                    } else {
                        throw new Exception("Undefined symbol in DATA: " + v);
                    }

                    String machineOctal = String.format("%06o", value & 0xFFFF);
                    String addrOctal = String.format("%06o", curAddr);

                    // Write to both files; for listing, keep original source line only for first item
                    OutputWriter.writeLine(bwListing, addrOctal, machineOctal, sourceLine);
                    OutputWriter.writeLine(bwLoad, addrOctal, machineOctal, null);
                    curAddr++;
                }
                continue;
            }

            if (type.equals("INST")) {
                // Parse the instruction using the complete symbol table
                Instruction instr = parser.parse(line, symTable);
                Opcode opcode = Opcode.fromMnemonic(instr.mnemonic);

                // Check for unresolved labels (which parser returns as -1)
                // This applies only to categories that use labels in the address field.
                if (instr.address == -1 && (
                    opcode.getCategory().equals("LoadStore") ||
                    opcode.getCategory().equals("ALU_Mem") ||
                    opcode.getCategory().equals("Transfer") ||
                    opcode.getCategory().equals("FloatVector")
                )) {
                    // Try to find what the label was for the error message
                    String addrTok = extractAddressToken(instr.original);
                    throw new Exception("Undefined symbol '" + addrTok + "' in line: " + raw);
                }

                String binary;

                // Assemble the 16-bit binary string based on the instruction's category
                switch (opcode.getCategory()) {

                    case "ALU_Reg":
                        // Format: Op(6) + Rx(2) + Ry(2) + Ignored(6)
                        // Parser stores Rx in R, Ry in IX
                        binary = opcode.getBinary()
                               + toBits(instr.R, 2)
                               + toBits(instr.IX, 2)
                               + "000000";
                        break;

                    case "ShiftRotate":
                        // Format: Op(6) + R(2) + A/L(1) + L/R(1) + Ign(2) + Count(4)
                        // Parser stores R in R, Count in address, (A/L, L/R) in IX
                        int al = (instr.IX >> 1) & 0x1; // Unpack A/L from IX
                        int lr = instr.IX & 0x1;        // Unpack L/R from IX
                        
                        binary = opcode.getBinary()
                               + toBits(instr.R, 2)
                               + toBits(al, 1)
                               + toBits(lr, 1)
                               + "00" // Ignored bits
                               + toBits(instr.address, 4); // Count
                        break;

                    case "Misc":
                        if (opcode.name().equals("HLT")) {
                            // Format: Op(6) + Ignored(10)
                            binary = "0000000000000000"; // HLT is all zeros
                        } else { // TRAP
                            // Format: Op(6) + Ignored(6) + Code(4)
                            binary = opcode.getBinary()
                                   + "000000" // Ignored bits
                                   + toBits(instr.address, 4); // Trap Code
                        }
                        break;

                    case "Transfer":
                        if (opcode.name().equals("RFS")) {
                            // Format: Op(6) + Ignored(5) + Immed(5)
                            binary = opcode.getBinary()
                                   + "00000" // Ignored R, IX, I
                                   + toBits(instr.address, 5); // Immed
                        } else if (opcode.name().equals("JMA")) {
                            // Format: Op(6) + Ignored(2) + IX(2) + I(1) + Addr(5)
                            binary = opcode.getBinary()
                                   + "00" // Ignored R
                                   + toBits(instr.IX, 2)
                                   + toBits(instr.I, 1)
                                   + toBits(instr.address, 5);
                        } else {
                            // Standard format for JZ, JNE, JCC, JSR, SOB, JGE
                            binary = opcode.getBinary()
                                   + toBits(instr.R, 2)
                                   + toBits(instr.IX, 2)
                                   + toBits(instr.I, 1)
                                   + toBits(instr.address, 5);
                        }
                        break;

                    default:
                        // Standard Format: Op(6) + R(2) + IX(2) + I(1) + Addr(5)
                        // This covers:
                        // - LoadStore
                        // - ALU_Mem
                        // - ALU_Immed (IX/I are ignored, parser sets them to 0)
                        // - IO (IX/I are ignored, parser sets them to 0)
                        // - FloatVector
                        binary = opcode.getBinary()
                               + toBits(instr.R, 2)
                               + toBits(instr.IX, 2)
                               + toBits(instr.I, 1)
                               + toBits(instr.address, 5);
                        break;
                }

                String machineOctal = String.format("%06o", Integer.parseInt(binary, 2));
                String addrOctal = String.format("%06o", addr);

                // Write to both files
                OutputWriter.writeLine(bwListing, addrOctal, machineOctal, sourceLine);
                OutputWriter.writeLine(bwLoad, addrOctal, machineOctal, null);
            }
        }

        bwListing.close();
        bwLoad.close();
    }

    private String toBits(int val, int bits) {
        String s = Integer.toBinaryString(val & ((1 << bits) - 1));
        while (s.length() < bits) s = "0" + s;
        return s.substring(s.length() - bits); // Ensure it's not too long
    }

    private String extractAddressToken(String original) {
        String[] parts = original.split("\\s+", 2);
        if (parts.length < 2) return "?";
        String[] ops = parts[1].replaceAll("\\s*,\\s*", ",").split(",");
        String addrTok = ops[ops.length - 1];
        return addrTok.replace("*", "").replace(",I", "").trim();
    }
}
