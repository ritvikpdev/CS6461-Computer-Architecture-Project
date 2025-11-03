package src;

public enum Opcode {
    // -------------------------------
    // Miscellaneous (Table 3)
    // -------------------------------
    HLT ("000000", "Misc"),   // Halt
    TRAP("011000", "Misc"),   // Trap with code

    // -------------------------------
    // Load / Store (Table 5)
    // -------------------------------
    LDR ("000001", "LoadStore"),   // Load Register from memory
    STR ("000010", "LoadStore"),   // Store Register to memory
    LDA ("000011", "LoadStore"),   // Load Effective Address
    LDX ("100001", "LoadStore"),   // Load Index Register
    STX ("100010", "LoadStore"),   // Store Index Register

    // -------------------------------
    // Transfer Instructions (Table 6)
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
    // Arithmetic & Logical (Table 7)
    // -------------------------------
    AMR ("000100", "ALU_Mem"),    // Add Memory to Register
    SMR ("000101", "ALU_Mem"),    // Subtract Memory from Register
    AIR ("000110", "ALU_Immed"),  // Add Immediate to Register
    SIR ("000111", "ALU_Immed"),  // Subtract Immediate from Register

    // -------------------------------
    // Register-to-Register (Table 8)
    // -------------------------------
    MLT ("111000", "ALU_Reg"),    // Multiply (Register to Register)
    DVD ("111001", "ALU_Reg"),    // Divide (Register to Register)
    TRR ("111010", "ALU_Reg"),    // Test Register-to-Register
    AND ("111011", "ALU_Reg"),    // Logical AND
    ORR ("111100", "ALU_Reg"),    // Logical OR
    NOT ("111101", "ALU_Reg"),    // Logical NOT

    // -------------------------------
    // Shift/Rotate (Table 9)
    // -------------------------------
    SRC ("011001", "ShiftRotate"), // Shift Register
    RRC ("011010", "ShiftRotate"), // Rotate Register

    // -------------------------------
    // I/O Instructions (Page 15)
    // -------------------------------
    IN  ("110001", "IO"),     // Input
    OUT ("110010", "IO"),     // Output
    CHK ("110011", "IO"),     // Check device status

    // -------------------------------
    // Floating Point/Vector (Table 10)
    // -------------------------------
    FADD  ("011011", "FloatVector"), // Floating Add
    FSUB  ("011100", "FloatVector"), // Floating Subtract
    VADD  ("011101", "FloatVector"), // Vector Add
    VSUB  ("011110", "FloatVector"), // Vector Subtract
    CNVRT ("011111", "FloatVector"), // Convert
    LDFR  ("101000", "FloatVector"), // Load Floating Register
    STFR  ("101001", "FloatVector"); // Store Floating Register


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