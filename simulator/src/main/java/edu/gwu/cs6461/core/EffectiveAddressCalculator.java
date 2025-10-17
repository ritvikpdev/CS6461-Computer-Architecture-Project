package edu.gwu.cs6461.core;

public final class EffectiveAddressCalculator {
    private EffectiveAddressCalculator(){}

    // Placeholder – you'll implement real EA logic (indexed + indirect) next.
    public static int computeEA(boolean indirect, int ix, int addr5, Registers regs, Memory mem){
        int ea = addr5 & 0x1F;
        if ((ix & 3) != 0) ea = (ea + regs.getX(ix)) & 0xFFFF;
        if (indirect) ea = mem.read(ea);
        return ea & 0xFFFF;
    }
}
