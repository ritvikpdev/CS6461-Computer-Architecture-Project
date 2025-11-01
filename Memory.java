public class Memory {
    private final int[] mem = new int[2048];  // 0..3777 (octal)

    public int read(int addr) {
        checkRange(addr);
        return mem[addr] & 0xFFFF;
    }

    public void write(int addr, int val) {
        checkRange(addr);
        mem[addr] = val & 0xFFFF;
    }

    private void checkRange(int addr) {
        if (addr < 0 || addr >= 2048)
            throw new IllegalArgumentException(
                String.format("Memory fault: illegal address %04o", addr));
    }

    public void clear() {
        for (int i = 0; i < mem.length; i++) mem[i] = 0;
    }
}
