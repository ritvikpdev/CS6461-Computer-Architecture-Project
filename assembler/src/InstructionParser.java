package src;

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

        // Use the opcode category to determine how to parse operands
        switch(opcode.getCategory()) {

            // Format: OP r, x, address[,I]
            // *** FIX: Special handling for LDX/STX ***
            case "LoadStore":
                if (mnemonic.equals("LDX") || mnemonic.equals("STX")) {
                    // Format: OP x, address[,I]
                    // We store 'x' in the R field, and set IX to 0
                    R = parseInt(ops[0]); // This is 'x'
                    IX = 0; // 'x' is not used in the traditional way
                    if (ops.length > 1) {
                        addr = parseAddressField(ops[1], symTable);
                        if (ops.length > 2 || ops[1].endsWith(",I")) I = 1;
                        if (ops.length > 2 && ops[2].equalsIgnoreCase("I")) I = 1;
                    }
                } else {
                    // Standard Format: OP r, x, address[,I]
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
                    if (ops.length > 3 || ops[2].endsWith(",I")) I = 1; // Simple ,I check
                    if (ops.length > 3 && ops[3].equalsIgnoreCase("I")) I = 1;
                }
                break;

            // Format: OP r, x, address[,I] (with exceptions)
            case "Transfer":
                if (mnemonic.equals("JMA")) { // Format: OP x, address[,I] (R is ignored)
                    R = 0; // R field is ignored
                    IX = (ops.length > 0) ? parseInt(ops[0]) : 0;
                    if (ops.length > 1) {
                         addr = parseAddressField(ops[1], symTable);
                         if (ops.length > 2 || ops[1].endsWith(",I")) I = 1;
                         if (ops.length > 2 && ops[2].equalsIgnoreCase("I")) I = 1;
                    }
                } else if (mnemonic.equals("RFS")) { // Format: OP Immed (Address field)
                    R = 0; IX = 0; I = 0; // All other fields ignored
                    if (ops.length > 0) addr = parseInt(ops[0]);
                } else { // Standard format for JZ, JNE, JCC, JSR, SOB, JGE
                    R = parseInt(ops[0]);
                    IX = (ops.length > 1) ? parseInt(ops[1]) : 0;
                    if (ops.length > 2) {
                        addr = parseAddressField(ops[2], symTable);
                        if (ops.length > 3 || ops[2].endsWith(",I")) I = 1;
                        if (ops.length > 3 && ops[3].equalsIgnoreCase("I")) I = 1;
                    }
                }
                break;
            
            // Format: OP r, immed
            case "ALU_Immed":
                R = parseInt(ops[0]);
                IX = 0; I = 0; // IX and I are ignored
                if (ops.length > 1) addr = parseInt(ops[1]); // Immed goes in Address field
                break;

            // Format: OP rx, ry
            case "ALU_Reg":
                R = parseInt(ops[0]);  // Mapped to Rx
                
                // *** FIX ***
                // NOT is a single-operand instruction
                if (mnemonic.equals("NOT")) {
                    IX = 0; // Ry is not used, set to 0
                } else {
                    // All other ALU_Reg instructions (TRR, AND, ORR, MLT, DVD)
                    // require a second operand (Ry)
                    if (ops.length < 2) {
                        throw new Exception("Missing second register operand for: " + mnemonic);
                    }
                    IX = parseInt(ops[1]); // Mapped to Ry
                }
                // *** END FIX ***
                
                I = 0; addr = 0;       // Rest are ignored
                break;

            // Format: OP r, count, L/R, A/L
            case "ShiftRotate":
                R = parseInt(ops[0]);
                int count = (ops.length > 1) ? parseInt(ops[1]) : 0;
                int lr = (ops.length > 2) ? parseInt(ops[2]) : 0;
                int al = (ops.length > 3) ? parseInt(ops[3]) : 0;
                
                // Pack A/L, L/R, and Count into the IX and Address fields
                IX = (al << 1) | lr;
                addr = count;
                I = 0; // I field is not used
                break;

            // Format: OP r, devid
            case "IO":
                R = parseInt(ops[0]);
                IX = 0; I = 0; // IX and I are ignored
                if (ops.length > 1) addr = parseInt(ops[1]); // DevID goes in Address field
                break;
            
            // Format: OP [trapcode]
            case "Misc":
                if (mnemonic.equals("TRAP") && ops.length > 0) {
                    addr = parseInt(ops[0]); // Trap code goes in Address field
                }
                // HLT has no operands, all fields remain 0
                break;
        }

        return new Instruction(mnemonic, R, IX, I, addr, line);
    }

    private int parseInt(String s) {
        return Integer.parseInt(s.trim());
    }

    /**
     * Parses an address field that could be an immediate number or a label.
     * Also handles the indirect flag '*'
     */
    private int parseAddressField(String token, SymbolTable symTable) {
        token = token.trim();
        
        // Handle indirection (e.g. *LOAD, *10)
        if (token.startsWith("*")) {
             token = token.substring(1);
        }

        if (token.matches("\\d+")) return Integer.parseInt(token); // It's a number
        if (symTable.contains(token)) return symTable.get(token); // It's a known label
        return -1; // Unresolved label (PassTwo will handle this)
    }
}

