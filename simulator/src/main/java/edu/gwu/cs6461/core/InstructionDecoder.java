package edu.gwu.cs6461.core;

public final class InstructionDecoder {
    private InstructionDecoder(){}

    // Format used: [15..10]=opcode(6) [9..8]=R(2) [7..6]=IX(2) [5]=I(1) [4..0]=ADDR(5)
    public static Decoded decode(int word){
        int opcode = (word >>> 10) & 0x3F;
        int r      = (word >>> 8)  & 0x03;
        int ix     = (word >>> 6)  & 0x03;
        boolean ind= ((word >>> 5) & 0x01) == 1;
        int addr5  =  word & 0x1F;
        return new Decoded(opcode, r, ix, ind, addr5);
    }

    public static int computeEA(Decoded d, Registers regs, Memory mem) throws edu.gwu.cs6461.core.util.MachineFaultException {
        int ea = d.addr5 & 0x1F;          // base address (5-bit)
        if (d.ix != 0) ea = (ea + regs.getX(d.ix)) & 0xFFFF;  // indexed
        if (d.indirect) ea = mem.read(ea);                    // indirect
        return ea & 0xFFFF;
    }

    public static final class Decoded {
        public final int opcode, r, ix, addr5;
        public final boolean indirect;
        public Decoded(int opcode, int r, int ix, boolean indirect, int addr5){
            this.opcode=opcode; this.r=r; this.ix=ix; this.indirect=indirect; this.addr5=addr5;
        }
    }
}
