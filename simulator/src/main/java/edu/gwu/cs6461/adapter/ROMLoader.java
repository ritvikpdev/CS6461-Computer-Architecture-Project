package edu.gwu.cs6461.adapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import edu.gwu.cs6461.core.Memory;
import edu.gwu.cs6461.core.Registers;
import edu.gwu.cs6461.core.util.MachineFaultException;

/**
 * Simple loader for a text "load file".
 * Accepts either:
 *   (a) "address value" pairs (space separated), OR
 *   (b) one value per line (auto-incrementing address).
 * Values can be binary(0b...), hex(0x...), octal(0o...), or plain decimal.
 */
public class ROMLoader {

    public int load(File file, Memory mem, Registers regs) throws IOException, MachineFaultException {
        int start = 0, nextAddr = 0;
        boolean first = true;

        try (BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;
            while((line = br.readLine()) != null){
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue;

                String[] tok = line.split("\\s+");
                int addr, val;

                if (tok.length == 1){
                    addr = nextAddr;
                    val  = parse(tok[0]);
                } else {
                    addr = parse(tok[0]);
                    val  = parse(tok[1]);
                    nextAddr = addr; // reset base
                }
                mem.write(addr, val);
                if (first){ start = addr; first = false; }
                nextAddr = (addr + 1) & 0xFFFF;
            }
        }
        regs.setPC(start);
        return start;
    }

    private static int parse(String s){
        String t = s.trim().toLowerCase();
        if (t.startsWith("0x")) return Integer.parseInt(t.substring(2), 16);
        if (t.startsWith("0b")) return Integer.parseInt(t.substring(2), 2);
        if (t.startsWith("0o")) return Integer.parseInt(t.substring(2), 8);
        if (t.matches("[01]{1,16}")) return Integer.parseInt(t, 2);
        if (t.matches("[0-7]+")) return Integer.parseInt(t, 8);
        if (t.matches("[0-9a-f]+")) return Integer.parseInt(t, 16);
        return Integer.parseInt(t, 10);
    }
}
