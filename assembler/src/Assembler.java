package src;

import java.util.*;

public class Assembler {
    public static void main(String[] args) {
        try {
String inputFile    = "CS6461-Computer-Architecture-Project/assembler/input/source.asm";
String listingFile  = "CS6461-Computer-Architecture-Project/assembler/output/listing.txt";
String loadFile     = "CS6461-Computer-Architecture-Project/assembler/output/loadfile.txt";

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
