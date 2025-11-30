package edu.gwu.cs6461.assembler;

public enum Opcode {
    HLT ("000000", "Misc"),
    TRAP("011000", "Misc"),
    LDR ("000001", "LoadStore"),
    STR ("000010", "LoadStore"),
    LDA ("000011", "LoadStore"),
    LDX ("100001", "LoadStore"),
    STX ("100010", "LoadStore"),
    JZ  ("001000", "Transfer"),
    JNE ("001001", "Transfer"),
    JCC ("001010", "Transfer"),
    JMA ("001011", "Transfer"),
    JSR ("001100", "Transfer"),
    RFS ("001101", "Transfer"),
    SOB ("001110", "Transfer"),
    JGE ("001111", "Transfer"),
    AMR ("000100", "ALU_Mem"),
    SMR ("000101", "ALU_Mem"),
    AIR ("000110", "ALU_Immed"),
    SIR ("000111", "ALU_Immed"),
    MLT ("111000", "ALU_Reg"),
    DVD ("111001", "ALU_Reg"),
    TRR ("111010", "ALU_Reg"),
    AND ("111011", "ALU_Reg"),
    ORR ("111100", "ALU_Reg"),
    NOT ("111101", "ALU_Reg"),
    SRC ("011001", "ShiftRotate"),
    RRC ("011010", "ShiftRotate"),
    IN  ("110001", "IO"),
    OUT ("110010", "IO"),
    CHK ("110011", "IO"),
    FADD  ("011011", "FloatVector"),
    FSUB  ("011100", "FloatVector"),
    VADD  ("011101", "FloatVector"),
    VSUB  ("011110", "FloatVector"),
    CNVRT ("011111", "FloatVector"),
    LDFR  ("101000", "FloatVector"),
    STFR  ("101001", "FloatVector");

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
