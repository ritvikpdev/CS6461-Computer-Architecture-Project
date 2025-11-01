public class Main {
    public static void main(String[] args) {
        Memory mem = new Memory();
        Cache cache = new Cache(mem, 8); // 8-line direct-mapped cache (as requested)
        CPU cpu = new CPU(mem, cache);
        ConsoleUI ui = new ConsoleUI(cpu, mem, cache);

        cpu.setOnStepUpdate(ui::refreshDisplay);
        ui.setVisible(true);
    }
}
