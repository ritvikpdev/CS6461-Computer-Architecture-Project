package edu.gwu.cs6461.io;

import java.util.function.Consumer;
import edu.gwu.cs6461.core.DecodedInstruction;
import edu.gwu.cs6461.core.Registers;

/**
 * Printer / console output device (device ID 1)
 */
public class PrinterDevice {
    private Consumer<String> outputConsumer;

    public void connect(Consumer<String> consumer) { this.outputConsumer = consumer; }

    public void output(DecodedInstruction d, Registers regs) {
        if (d.address() == 1 && outputConsumer != null) {
            outputConsumer.accept(String.valueOf((short) regs.getGPR(d.r())));
        }
    }
}
