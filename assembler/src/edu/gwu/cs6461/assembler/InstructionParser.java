package edu.gwu.cs6461.assembler;

/**
 * Parses assembly lines into Instruction objects.
 * This version is updated to handle the specific operand formats
 * for each instruction category from the C6461 ISA PDF.
 */
public class InstructionParser {

    public Instruction parse(String line, SymbolTable symTable) throws Exception {
        String[] parts = line.trim().split("\\s+", 2);
        String mnemonic = parts[0].toUpperCase();
        Opcode opcode = Opcode.fromMnemonic(mnemonic);
        if (opcode == null) throw new Exception("Unknown opcode: " + mnemonic);

        int R = 0, IX = 0, I = 0, addr = 0;
        String[] ops = new String[0];
        if (parts.length > 1) {
            ops = parts[1].replaceAll("\\s*,\\s*", ",").split(",");
        }

        switch(opcode.getCategory()) {
            case "LoadStore":
                if (mnemonic.equals("LDX") || mnemonic.equals("STX")) {
                    R = parseInt(ops[0]);
                    IX = 0;
                    if (ops.length > 1) {
                        addr = parseAddressField(ops[1], symTable);
                        if (ops.length > 2 || ops[1].endsWith(",I")) I = 1;
                        if (ops.length > 2 && ops[2].equalsIgnoreCase("I")) I = 1;
                    }
                } else {
                    R = parseInt(ops[0]);
                    IX = (ops.length > 1) ? parseInt(ops[1]) : 0;
                    if (ops.length > 2) {
                        addr = parseAddressField(ops[2], symTable);
                        if (ops.length > 3 || ops[2].endsWith(",I")) I = 1;
                        if (ops.length > 3 && ops[3].equalsIgnoreCase("I")) I = 1;
                    }
                }
                break;
            case "ALU_Mem":
            case "FloatVector":
                R = parseInt(ops[0]);
                IX = (ops.length > 1) ? parseInt(ops[1]) : 0;
                if (ops.length > 2) {
                    addr = parseAddressField(ops[2], symTable);
                    if (ops.length > 3 || ops[2].endsWith(",I")) I = 1;
                    if (ops.length > 3 && ops[3].equalsIgnoreCase("I")) I = 1;
                }
                break;
            case "Transfer":
                if (mnemonic.equals("JMA")) {
                    R = 0;
                    IX = (ops.length > 0 && isNumber(ops[0])) ? parseInt(ops[0]) : 0;
                    if (ops.length > 1) {
                         addr = parseAddressField(ops[1], symTable);
                         if (ops.length > 2 || ops[1].endsWith(",I")) I = 1;
                         if (ops.length > 2 && ops[2].equalsIgnoreCase("I")) I = 1;
                    } else if (ops.length > 0 && !isNumber(ops[0])) {
                         addr = parseAddressField(ops[0], symTable);
                    }
                } else if (mnemonic.equals("RFS")) {
                    R = 0; IX = 0; I = 0;
                    if (ops.length > 0) addr = parseInt(ops[0]);
                } else {
                    // Standard format for JZ, JNE, JCC, JSR, SOB, JGE
                    R = parseInt(ops[0]);
                    // If the next operand is not numeric, it's actually the address and IX is omitted
                    if (ops.length > 1 && isNumber(ops[1])) {
                        IX = parseInt(ops[1]);
                    } else {
                        IX = 0;
                    }
                    // Address may be the 3rd operand (when IX present) or the 2nd (when IX omitted)
                    if (ops.length > 2) {
                        addr = parseAddressField(ops[2], symTable);
                        if (ops.length > 3 || ops[2].endsWith(",I")) I = 1;
                        if (ops.length > 3 && ops[3].equalsIgnoreCase("I")) I = 1;
                    } else if (ops.length > 1 && !isNumber(ops[1])) {
                        addr = parseAddressField(ops[1], symTable);
                        if (ops[1].endsWith(",I")) I = 1;
                    }
                }
                break;
            case "ALU_Immed":
                R = parseInt(ops[0]);
                IX = 0; I = 0;
                if (ops.length > 1) addr = parseInt(ops[1]);
                break;
            case "ALU_Reg":
                R = parseInt(ops[0]);
                if (mnemonic.equals("NOT")) {
                    IX = 0;
                } else {
                    if (ops.length < 2) {
                        throw new Exception("Missing second register operand for: " + mnemonic);
                    }
                    IX = parseInt(ops[1]);
                }
                I = 0; addr = 0;
                break;
            case "ShiftRotate":
                R = parseInt(ops[0]);
                int count = (ops.length > 1) ? parseInt(ops[1]) : 0;
                int lr = (ops.length > 2) ? parseInt(ops[2]) : 0;
                int al = (ops.length > 3) ? parseInt(ops[3]) : 0;
                IX = (al << 1) | lr;
                addr = count;
                I = 0;
                break;
            case "IO":
                R = parseInt(ops[0]);
                IX = 0; I = 0;
                if (ops.length > 1) addr = parseInt(ops[1]);
                break;
            case "Misc":
                if (mnemonic.equals("TRAP") && ops.length > 0) {
                    addr = parseInt(ops[0]);
                }
                break;
        }
        return new Instruction(mnemonic, R, IX, I, addr, line);
    }

    private int parseInt(String s) {
        return Integer.parseInt(s.trim());
    }

    private boolean isNumber(String s) {
        return s != null && s.trim().matches("\\d+");
    }

    private int parseAddressField(String token, SymbolTable symTable) {
        token = token.trim();
        if (token.startsWith("*")) {
             token = token.substring(1);
        }
        if (token.matches("\\d+")) return Integer.parseInt(token);
        if (symTable.contains(token)) return symTable.get(token);
        return -1;
    }
}
