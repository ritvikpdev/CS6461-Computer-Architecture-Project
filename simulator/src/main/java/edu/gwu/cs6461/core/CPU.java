package edu.gwu.cs6461.core;

import java.util.function.Consumer;
import java.util.function.Supplier;
import edu.gwu.cs6461.io.ConsoleDevice;
import edu.gwu.cs6461.io.PrinterDevice;


/**
 * Central control unit that manages fetch–decode–execute cycle.
 */
public class CPU {
    private final Registers regs = new Registers();
    private final Memory mem;
    private final InstructionDecoder decoder = new InstructionDecoder();
    private final Executor executor;
    private final ConsoleDevice console;
    private final PrinterDevice printer;
    private boolean halted = false;

    public CPU(Memory mem) {
        this(mem, new ConsoleDevice(), new PrinterDevice());
    }

    public CPU(Memory mem, ConsoleDevice console, PrinterDevice printer) {
        this.mem = mem;
        this.console = console;
        this.printer = printer;
        this.executor = new Executor(mem, regs, console, printer);
        regs.reset();
    }

    // Control
    public void reset() { regs.reset(); halted = false; }
    public void halt() { halted = true; }
    public void unhalt() { halted = false; }
    public boolean isHalted() { return halted; }
    public Registers getRegs() { return regs; }

    // Single step
    public void step() {
        if (halted) return;
        int ir = fetch();
        DecodedInstruction d = decoder.decode(ir);
        executor.execute(d, this);
    }

    // Run loop (simple cooperative loop)
    public void run(Runnable onStep) {
        Thread t = new Thread(() -> {
            int guard = 1_000_000; // safety guard to avoid infinite loops
            while (!halted && guard-- > 0) {
                step();
                if (onStep != null) {
                    try { onStep.run(); } catch (Throwable ignore) {}
                }
                try { Thread.sleep(1); } catch (InterruptedException ignored) {}
            }
        }, "cpu-runner");
        t.setDaemon(true);
        t.start();
    }

    // Memory operations for manual UI buttons
    public void manual_load() {
        regs.setMBR(mem.getValueAt(regs.getMAR()));
    }

    public void manual_load_plus() {
        manual_load();
        regs.setMAR((regs.getMAR() + 1) & 0xFFF);
    }

    public void manual_store() {
        mem.setValueAt(regs.getMAR(), (short) regs.getMBR());
    }

    public void manual_store_plus() {
        manual_store();
        regs.setMAR((regs.getMAR() + 1) & 0xFFF);
    }

    // Register accessors for UI
    public int getGPR(int i) { return regs.getGPR(i); }
    public void setGPR(int i, int v) { regs.setGPR(i, v); }
    public int getIXR(int i) { return regs.getIXR(i); }
    public void setIXR(int i, int v) { regs.setIXR(i, v); }
    public int getPC() { return regs.getPC(); }
    public void setPC(int v) { regs.setPC(v); }
    public int getIR() { return regs.getIR(); }
    public void setIR(int v) { regs.setIR(v); }
    public int getMAR() { return regs.getMAR(); }
    public void setMAR(int v) { regs.setMAR(v); }
    public int getMBR() { return regs.getMBR(); }
    public void setMBR(int v) { regs.setMBR(v); }
    public int getCC() { return regs.getCC(); }
    public void setCC(int v) { regs.setCC(v); }
    public int getMFR() { return regs.getMFR(); }
    public void setMFR(int v) { regs.setMFR(v); }

    // I/O wiring for UI
    public void setConsoleInputSupplier(Supplier<Integer> supplier) { console.connect(supplier); }
    public void setPrinterConsumer(Consumer<String> consumer) { printer.connect(consumer); }

    private int fetch() {
        regs.setMAR(regs.getPC());
        regs.setMBR(mem.getValueAt(regs.getMAR()));
        regs.setIR(regs.getMBR());
        regs.incrementPC();
        return regs.getIR();
    }
}
