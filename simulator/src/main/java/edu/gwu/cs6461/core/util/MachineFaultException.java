package edu.gwu.cs6461.core.util;
public class MachineFaultException extends Exception {
    private final FaultType type;
    public MachineFaultException(FaultType type, String message){ super(message); this.type = type; }
    public FaultType getType(){ return type; }
}
