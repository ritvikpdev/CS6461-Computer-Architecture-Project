package edu.gwu.cs6461.assembler;

import java.io.*;

public class OutputWriter {
    public static void writeLine(BufferedWriter bw, String addrOctal, String machineOctal, String source) throws IOException {
        if (source != null) {
            bw.write(String.format("%6s   %6s   %s", addrOctal, machineOctal, source));
        } else {
            bw.write(String.format("%6s   %6s", addrOctal, machineOctal));
        }
        bw.newLine();
    }
}
