package edu.gwu.cs6461.core;

import edu.gwu.cs6461.io.ConsoleDevice;
import edu.gwu.cs6461.io.PrinterDevice;

/**
 * Executes decoded instructions and interacts with memory, ALU, and I/O.
 */
public class Executor {
    private final Memory mem;
    private final Registers regs;
    private final ArithmeticUnit alu;
    private final ConsoleDevice console;
    private final PrinterDevice printer;

    public Executor(Memory mem, Registers regs, ConsoleDevice console, PrinterDevice printer) {
        this.mem = mem;
        this.regs = regs;
        this.alu = new ArithmeticUnit(regs);
        this.console = console;
        this.printer = printer;
    }

    public void execute(DecodedInstruction d, CPU cpu) {
        switch (d.opcode()) {
            case 0 -> cpu.halt();
            case 1 -> load(d);
            case 2 -> store(d);
            case 4 -> alu.memoryOp(d, mem, '+');
            case 5 -> alu.memoryOp(d, mem, '-');
            case 6 -> immediate(d, '+');
            case 7 -> immediate(d, '-');
            case 20,21,22,23,24,25 -> alu.registerOp(d, cpu);
            case 61 -> console.input(d, regs);
            case 62 -> printer.output(d, regs);
            default -> FaultHandler.illegalOpcode(cpu, d.opcode());
        }
    }

    private void load(DecodedInstruction d) {
        int ea = getEA(d);
        regs.setGPR(d.r(), mem.getValueAt(ea));
    }

    private void store(DecodedInstruction d) {
        int ea = getEA(d);
        mem.setValueAt(ea, (short) regs.getGPR(d.r()));
    }

    private void immediate(DecodedInstruction d, char op) {
        int imm = d.address() & 0xFF;
        if ((imm & 0x80) != 0) imm |= 0xFF00;
        int val = (op == '+') ? regs.getGPR(d.r()) + imm : regs.getGPR(d.r()) - imm;
        regs.setGPR(d.r(), val);
    }

    private int getEA(DecodedInstruction d) {
        int ea = (d.ix() == 0) ? d.address() : d.address() + regs.getIXR(d.ix());
        if (d.i() == 1) ea = mem.getValueAt(ea);
        return ea & 0xFFF;
    }
}
