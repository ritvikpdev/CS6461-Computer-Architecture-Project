public class Cache {
    private final Memory mem;
    private final CacheLine[] lines;
    private final int size;       // number of lines (power of two)
    private int hits = 0;
    private int misses = 0;

    public Cache(Memory mem, int linesCount) {
        if (linesCount <= 0 || (linesCount & (linesCount - 1)) != 0) {
            throw new IllegalArgumentException("Cache size must be a power of two.");
        }
        this.mem = mem;
        this.size = linesCount;
        this.lines = new CacheLine[size];
        for (int i = 0; i < size; i++) lines[i] = new CacheLine();
    }

    private int indexOf(int addr) {
        return addr & (size - 1); // modulo size when size is power of two
    }
    private int tagOf(int addr) {
        int shift = Integer.numberOfTrailingZeros(size);
        return addr >>> shift;
    }

    public int read(int addr) {
        addr &= 0xFFFF;
        int idx = indexOf(addr);
        int tag = tagOf(addr);
        CacheLine line = lines[idx];

        if (line.valid && line.tag == tag) {
            hits++;
            System.out.printf("CACHE HIT  @ %04o  [idx=%02o, tag=%06o]\n", addr, idx, tag);
            return line.data & 0xFFFF;
        } else {
            misses++;
            System.out.printf("CACHE MISS @ %04o  [idx=%02o, tag=%06o]\n", addr, idx, tag);
            int val = mem.read(addr) & 0xFFFF;
            // write-allocate: fill line
            line.valid = true;
            line.tag = tag;
            line.address = addr;
            line.data = val;
            return val;
        }
    }

    public void write(int addr, int val) {
        addr &= 0xFFFF;
        val  &= 0xFFFF;
        int idx = indexOf(addr);
        int tag = tagOf(addr);
        CacheLine line = lines[idx];

        // write-allocate + write-through
        line.valid = true;
        line.tag = tag;
        line.address = addr;
        line.data = val;

        mem.write(addr, val); // write-through to main memory
    }

    public void clear() {
        for (CacheLine cl : lines) cl.invalidate();
        hits = 0;
        misses = 0;
    }

    public int getHits() { return hits; }
    public int getMisses() { return misses; }
    public double getHitRate() {
        int total = hits + misses;
        return total == 0 ? 0.0 : (hits * 1.0) / total;
    }

    public int getSize() { return size; }
}
