package edu.gwu.cs6461.assembler;

import java.util.*;

public class Assembler {
    public static void main(String[] args) {
        try {
            // Base path relative to project root
            String base = "assembler";
            String inputFile    = base + "/input/source.asm";
            String listingFile  = base + "/output/listing.txt";
            String loadFile     = base + "/output/loadfile.txt";

            SymbolTable symTable = new SymbolTable();
            PassOne passOne = new PassOne();
            PassTwo passTwo = new PassTwo();

            // First pass → collect symbols + program
            List<String[]> program = passOne.firstPass(inputFile, symTable);

            // Second pass → generate both outputs
            passTwo.secondPass(program, symTable, listingFile, loadFile);

            System.out.println("Assembler executed. See:");
            System.out.println("   Listing file: " + listingFile);
            System.out.println("   Load file   : " + loadFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
