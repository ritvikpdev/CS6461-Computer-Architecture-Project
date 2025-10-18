package edu.gwu.cs6461.core;

public class SimulatorCore {
    public final Registers regs = new Registers();
    public final Memory mem = new Memory(2048);
    public final CPU cpu = new CPU(regs, mem);
}
