package src;

import java.io.*;
import java.util.*;

public class PassTwo {
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
            String sourceLine = (label != null ? label + ": " : "") + raw.trim();

            if (type.equals("LOC")) {
                continue; // no machine code generated
            }

            if (type.equals("DATA")) {
                String[] parts = line.split("\\s+");
                String operand = parts[1];
                int value;

                if (operand.matches("\\d+")) {
                    value = Integer.parseInt(operand);
                } else if (symTable.contains(operand)) {
                    value = symTable.get(operand);
                } else {
                    throw new Exception("Undefined symbol in DATA: " + operand);
                }

                String machineOctal = String.format("%06o", value & 0xFFFF);
                String addrOctal = String.format("%06o", addr);

                // Write to both files
                OutputWriter.writeLine(bwListing, addrOctal, machineOctal, sourceLine);
                OutputWriter.writeLine(bwLoad, addrOctal, machineOctal, null);
                continue;
            }

            if (type.equals("INST")) {
                Instruction instr = parser.parse(line, symTable);

                if (instr.address == -1) {
                    String addrTok = extractAddressToken(instr.original);
                    if (symTable.contains(addrTok)) {
                        instr.address = symTable.get(addrTok);
                    } else {
                        throw new Exception("Undefined symbol: " + addrTok);
                    }
                }

                String binary = instr.getOpcodeBinary()
                        + toBits(instr.R, 2)
                        + toBits(instr.IX, 2)
                        + toBits(instr.I, 1)
                        + toBits(instr.address, 5);

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
        return s;
    }

    private String extractAddressToken(String original) {
        String[] parts = original.split("\\s+", 2);
        if (parts.length < 2) return null;
        String[] ops = parts[1].replaceAll("\\s*,\\s*", ",").split(",");
        return (ops.length >= 3) ? ops[2].replace("*", "").trim() : null;
    }
}
