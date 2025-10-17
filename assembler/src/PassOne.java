package src;

import java.io.*;
import java.util.*;

public class PassOne {

    /**
     * Returns program lines as list: {address, type, line, raw, label}
     */
    public List<String[]> firstPass(String inputFile, SymbolTable symTable) throws Exception {
        List<String[]> program = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(inputFile));

        int locctr = 0;
        boolean started = false;
        String raw;

        while ((raw = br.readLine()) != null) {
            String line = stripComments(raw).trim();
            if (line.isEmpty()) continue;

            // ------------------------
            // Handle labels
            // ------------------------
            String label = null;
            if (line.contains(":")) {
                int idx = line.indexOf(':');
                label = line.substring(0, idx).trim();
                line = line.substring(idx + 1).trim();
            }

            String[] tokens = line.split("\\s+");
            String firstToken = tokens[0].toUpperCase();

            // ------------------------
            // Handle LOC (set address)
            // ------------------------
            if (!started) {
                if (firstToken.equals("LOC")) {
                    locctr = Integer.parseInt(tokens[1]);
                    started = true;
                    if (label != null) symTable.add(label, locctr);
                    continue;
                } else {
                    started = true;
                }
            }

            if (label != null) symTable.add(label, locctr);

            if (firstToken.equals("END")) break;

            if (firstToken.equals("LOC")) {
                locctr = Integer.parseInt(tokens[1]);
                program.add(new String[]{String.valueOf(locctr), "LOC", line, raw, label});
                continue;
            }

            if (firstToken.equalsIgnoreCase("DATA")) {
                program.add(new String[]{String.valueOf(locctr), "DATA", line, raw, label});
                locctr++;
                continue;
            }

            // Instruction
            program.add(new String[]{String.valueOf(locctr), "INST", line, raw, label});
            locctr++;
        }
        br.close();
        return program;
    }

    private String stripComments(String s) {
        int idx = s.indexOf(';');
        if (idx < 0) idx = s.indexOf('#');
        if (idx < 0) idx = s.indexOf("//");
        return (idx >= 0) ? s.substring(0, idx) : s;
    }
}