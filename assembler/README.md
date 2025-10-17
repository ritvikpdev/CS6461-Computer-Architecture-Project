# 🖥️ Two-Pass Assembler (Java)

## 📌 Project Overview
This project implements a **two-pass assembler** in Java based on the instruction set described in class notes.  
The assembler will:
- Take an **assembly source file** as input (`input/source.asm`).
- Perform **Pass One** to build a symbol table (labels → addresses).
- Perform **Pass Two** to generate **16-bit machine instructions**.
- Convert instructions to **octal representation**.
- Output a **load file** (`output/loadfile.txt`) with the format:

### Example:
Input (`source.asm`):

ORG 10
LDR 3,0,15
STR 2,1,20
LDA 1,2,31
END

Output (`loadfile.txt`):

000012 003417 LDR 3,0,15
000013 024524 STR 2,1,20
000014 033174 LDA 1,2,31

------------------------------

## 📂 Project Structure

AssemblerProject/
│── src/
│ ├── assembler/
│ │ ├── Assembler.java # Main driver
│ │ ├── PassOne.java # First pass - build symbol table
│ │ ├── PassTwo.java # Second pass - generate machine code
│ │ ├── SymbolTable.java # Symbol table class
│ │ ├── OpcodeTable.java # Mnemonics → binary opcode map
│ │ ├── Instruction.java # Instruction object
│ │ ├── InstructionParser.java # Parses assembly lines
│ │ ├── OutputWriter.java # Writes results to output file
│── input/
│ ├── source.asm # Input assembly program
│── output/
│ ├── loadfile.txt # Output file (generated)
│── README.md # Project description


--------------------------------

## ⚙️ How to Run
1. Clone the repository:
   - git clone https://github.com/ritvikm10/CS6461-Computer-Architecture-Project.git
2. Put one block at a time into input/source.asm.
3. Run:
   javac src/assembler/*.java
   java -cp src assembler.Assembler


Check:

output/listing.txt → human-readable listing file.

output/loadfile.txt → machine-loadable memory file.
   
 --------------------------------
 
## Modules & Responsibilities: 
- Assembler.java → Main driver, coordinates passes.
- PassOne.java → Builds SymbolTable.
- SymbolTable.java → Stores label → address mappings.
- PassTwo.java → Translates instructions to machine code.
- OpcodeTable.java → Provides opcode → binary mapping.
- InstructionParser.java → Converts source lines into Instruction objects.
- OutputWriter.java → Formats output into octal load file.
   
