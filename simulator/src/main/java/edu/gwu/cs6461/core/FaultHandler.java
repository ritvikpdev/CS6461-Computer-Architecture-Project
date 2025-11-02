package edu.gwu.cs6461.core;

public class FaultHandler {
    public static final int ILLEGAL_OPCODE = 0x1;
    public static final int DIV_ZERO = 0x2;
    public static final int MEM_FAULT = 0x4;

    public static void illegalOpcode(CPU cpu, int opcode) {
        cpu.getRegs().setMFR(ILLEGAL_OPCODE);
        System.err.println("Illegal opcode: " + opcode);
        cpu.halt();
    }

    public static void divZero(CPU cpu) {
        cpu.getRegs().setMFR(DIV_ZERO);
        System.err.println("Divide by zero fault");
        cpu.halt();
    }

    public static void memoryFault(CPU cpu, String msg) {
        cpu.getRegs().setMFR(MEM_FAULT);
        System.err.println("Memory fault: " + msg);
        cpu.halt();
    }
}
