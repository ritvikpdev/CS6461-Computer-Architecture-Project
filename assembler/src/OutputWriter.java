package src;

import java.io.*;

public class OutputWriter {
    public static void writeLine(BufferedWriter bw, String addrOctal, String machineOctal, String source) throws IOException {
        if (source != null) {
            // Listing file: addr + code + source
            bw.write(String.format("%6s   %6s   %s", addrOctal, machineOctal, source));
        } else {
            // Load file: addr + code only
            bw.write(String.format("%6s   %6s", addrOctal, machineOctal));
        }
        bw.newLine();
    }
}
