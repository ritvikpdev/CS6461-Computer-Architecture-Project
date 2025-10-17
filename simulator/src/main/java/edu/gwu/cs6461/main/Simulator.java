package edu.gwu.cs6461.main;

import edu.gwu.cs6461.core.CPU;
import edu.gwu.cs6461.core.Memory;
import edu.gwu.cs6461.core.Registers;

public class Simulator {
    public static void main(String[] args) {
        System.out.println("=== CS6461 Simulator Boot ===");
        Memory memory = new Memory(2048);
        Registers regs = new Registers();
        CPU cpu = new CPU(regs, memory);
        System.out.println("Skeleton up. Next: load program and step CPU.");
    }
}
