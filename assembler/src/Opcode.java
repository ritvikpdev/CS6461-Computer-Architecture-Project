package src;

public enum Opcode {
    // -------------------------------
    // Miscellaneous
    // -------------------------------
    HLT ("000000", "Misc"),   // Halt
    TRAP("000001", "Misc"),   // Trap with code

    // -------------------------------
    // Load / Store
    // -------------------------------
    LDR ("000010", "LoadStore"),   // Load Register from memory
    STR ("000011", "LoadStore"),   // Store Register to memory
    LDA ("000100", "LoadStore"),   // Load Effective Address
    LDX ("000101", "LoadStore"),   // Load Index Register
    STX ("000110", "LoadStore"),   // Store Index Register

    // -------------------------------
    // Transfer Instructions
    // -------------------------------
    JZ  ("001000", "Transfer"),   // Jump if zero
    JNE ("001001", "Transfer"),   // Jump if not equal
    JCC ("001010", "Transfer"),   // Jump if condition code
    JMA ("001011", "Transfer"),   // Unconditional jump
    JSR ("001100", "Transfer"),   // Jump to subroutine
    RFS ("001101", "Transfer"),   // Return from subroutine
    SOB ("001110", "Transfer"),   // Subtract One & Branch
    JGE ("001111", "Transfer"),   // Jump if greater/equal

    // -------------------------------
    // Arithmetic & Logical
    // -------------------------------
    AMR ("010000", "ALU"),   // Add Memory to Register
    SMR ("010001", "ALU"),   // Subtract Memory from Register
    AIR ("010010", "ALU"),   // Add Immediate to Register
    SIR ("010011", "ALU"),   // Subtract Immediate from Register
    MLT ("010100", "ALU"),   // Multiply (Register to Register)
    DVD ("010101", "ALU"),   // Divide (Register to Register)
    TRR ("010110", "ALU"),   // Test Register-to-Register
    AND ("010111", "ALU"),   // Logical AND
    ORR ("011000", "ALU"),   // Logical OR
    NOT ("011001", "ALU"),   // Logical NOT
    SRC ("011010", "ALU"),   // Shift Register
    RRC ("011011", "ALU"),   // Rotate Register

    // -------------------------------
    // I/O Instructions
    // -------------------------------
    IN  ("011100", "IO"),    // Input
    OUT ("011101", "IO"),    // Output
    CHK ("011110", "IO");    // Check device status

    // -------------------------------
    // Fields
    // -------------------------------
    private final String binary;
    private final String category;

    Opcode(String binary, String category) {
        this.binary = binary;
        this.category = category;
    }

    public String getBinary() { return binary; }
    public String getCategory() { return category; }

    public static Opcode fromMnemonic(String mnemonic) {
        for (Opcode op : values()) {
            if (op.name().equalsIgnoreCase(mnemonic)) return op;
        }
        return null;
    }
}