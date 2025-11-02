package edu.gwu.cs6461.io;

import java.util.function.Supplier;
import edu.gwu.cs6461.core.DecodedInstruction;
import edu.gwu.cs6461.core.Registers;

/**
 * Keyboard input device (device ID 0)
 */
public class ConsoleDevice {
    private Supplier<Integer> inputSupplier;

    public void connect(Supplier<Integer> supplier) { this.inputSupplier = supplier; }

    public void input(DecodedInstruction d, Registers regs) {
        if (d.address() == 0 && inputSupplier != null) {
            int val = inputSupplier.get();
            if (val != -1) regs.setGPR(d.r(), val);
        }
    }
}
