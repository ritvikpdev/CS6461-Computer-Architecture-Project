package edu.gwu.cs6461.core;

public class CPU {
    private final Registers regs;
    private final Memory mem;

    public CPU(Registers regs, Memory mem) {
        this.regs = regs;
        this.mem = mem;
    }

    public void step() {
        // TODO: fetch-decode-execute
        System.out.println("[CPU] step() not implemented yet");
    }
}
