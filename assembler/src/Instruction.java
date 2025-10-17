package src;

/**
 * Represents a parsed instruction.
 */
public class Instruction {
    public String mnemonic;
    public int R, IX, I, address;
    public String original;

    public Instruction(String mnemonic, int R, int IX, int I, int address, String original) {
        this.mnemonic = mnemonic;
        this.R = R;
        this.IX = IX;
        this.I = I;
        this.address = address;
        this.original = original;
    }

    public String getOpcodeBinary() {
        Opcode op = Opcode.fromMnemonic(mnemonic);
        return (op != null) ? op.getBinary() : null;
    }
}