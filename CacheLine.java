public class CacheLine {
    boolean valid;
    int tag;
    int address;
    int data;

    public CacheLine() {
        valid = false;
        tag = 0;
        address = 0;
        data = 0;
    }

    public void invalidate() {
        valid = false;
    }
}
