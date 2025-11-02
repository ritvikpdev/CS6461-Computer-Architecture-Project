package edu.gwu.cs6461.core;

/**
 * Represents a decoded instruction with all fields extracted.
 */
public record DecodedInstruction(int opcode, int r, int ix, int i, int address) {}
