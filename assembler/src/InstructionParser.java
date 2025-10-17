package src;

/**
 * Parses assembly lines into Instruction objects.
 */
public class InstructionParser {

    public Instruction parse(String line, SymbolTable symTable) throws Exception {
        String[] parts = line.trim().split("\\s+", 2);
        String mnemonic = parts[0].toUpperCase();
        Opcode opcode = Opcode.fromMnemonic(mnemonic);
        if (opcode == null) throw new Exception("Unknown opcode: " + mnemonic);

        int R = 0, IX = 0, I = 0, addr = 0;

        if (parts.length > 1) {
            String[] ops = parts[1].replaceAll("\\s*,\\s*", ",").split(",");
            switch(opcode.getCategory()) {
                case "LoadStore":
                case "Transfer":
                case "ALU":
                    R = parseInt(ops[0]);
                    IX = (ops.length > 1) ? parseInt(ops[1]) : 0;
                    if (ops.length > 2) {
                        String addrTok = ops[2];
                        if (addrTok.startsWith("*")) {
                            I = 1;
                            addrTok = addrTok.substring(1);
                        }
                        addr = resolveAddress(addrTok, symTable);
                    }
                    break;

                case "Misc":
                    if (mnemonic.equals("TRAP") && ops.length > 0) {
                        addr = parseInt(ops[0]); // trap code
                    }
                    break;

                case "IO":
                    R = parseInt(ops[0]);
                    addr = parseInt(ops[1]); // device id
                    break;
            }
        }

        return new Instruction(mnemonic, R, IX, I, addr, line);
    }

    private int parseInt(String s) {
        return Integer.parseInt(s.trim());
    }

    private int resolveAddress(String token, SymbolTable symTable) {
        if (token.matches("\\d+")) return Integer.parseInt(token);
        if (symTable.contains(token)) return symTable.get(token);
        return -1; // unresolved label
    }
}