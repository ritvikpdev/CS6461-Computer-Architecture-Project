package src;

import java.util.*;

public class Assembler {
    public static void main(String[] args) {
        try {
            String inputFile    = "input/source.asm";
            String listingFile  = "output/listing.txt";  // Figure 4
            String loadFile     = "output/loadfile.txt"; // Figure 5

            SymbolTable symTable = new SymbolTable();
            PassOne passOne = new PassOne();
            PassTwo passTwo = new PassTwo();

            // First pass → collect symbols + program
            List<String[]> program = passOne.firstPass(inputFile, symTable);

            // Second pass → generate both outputs
            passTwo.secondPass(program, symTable, listingFile, loadFile);

            System.out.println("Assembler executed. See:");
            System.out.println("   Listing file:" + listingFile);
            System.out.println("   Load file   : " + loadFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
