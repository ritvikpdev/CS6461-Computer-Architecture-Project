package edu.gwu.cs6461.assembler;

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

            
            // Handle labels
            String label = null;
            if (line.contains(":")) {
                int idx = line.indexOf(':');
                label = line.substring(0, idx).trim();
                line = line.substring(idx + 1).trim();
            }

            // Handle LOC first, as it changes locctr 
            String[] tokens = line.split("\\s+");
            String firstToken = line.isEmpty() ? "" : tokens[0].toUpperCase();

            if (firstToken.equals("LOC")) {
                locctr = Integer.parseInt(tokens[1]);
                if (!started) {
                    started = true;
                }
                
                // If there was a label, add it with the NEW locctr
                if (label != null) {
                    symTable.add(label, locctr);
                }
                
                // Add to program list but don't increment locctr
                program.add(new String[]{String.valueOf(locctr), "LOC", line, raw, label});
                continue; // Move to next line
            }

            // If we are here, it's not a LOC line.
            // Add any label with the CURRENT locctr.
            if (label != null) {
                symTable.add(label, locctr);
            }

            // If the line was *only* a label, we are done.
            if (line.isEmpty()) {
                continue; 
            }
            
            // Handle starting point (if not set by LOC)
            if (!started) {
                started = true;
            }

            if (firstToken.equals("END")) break;

            
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
