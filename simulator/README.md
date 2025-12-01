# CS6461 Basic Machine Simulator (Swing)

A Swing-based simulator for the CS6461 Basic Machine. It provides a GUI to view registers and cache, load and execute programs, and interact via console input/output.

**Current Program**: Program 2 - Paragraph Word Search
- Reads a 6-sentence paragraph from memory
- Displays the sentences on console printer
- Accepts a search word from the user
- Searches and reports word location (sentence number and position)



## Requirements

- Java 21 (e.g., Eclipse Adoptium JDK 21)
- Maven 3.8+
- Windows PowerShell commands are shown below; adjust for your shell if different.

## Quick Start Example

powershell
 1. Build the simulator
cd .\simulator
mvn clean package -DskipTests

 2. Run the simulator
   java -jar .\target\CS6461Simulator-1.0.0.jar
   or if jar present in root folder
    java -jar \CS6461Simulator-1.0.0.jar

 3. In the GUI:
    - Click IPL (load Program2.txt )
    - Click Run (displays paragraph sentences)
    - Wait for "Enter word to search:" prompt
    - Type search word in Console Input
    - Press Enter
    - View results in Printer Output


## Using the GUI

### Main Controls (Top Toolbar)

- **IPL (Initial Program Load)**: Initialize and load a program
  - File chooser opens in the simulator folder
  - Default: `Program2.txt` (automatically preselected)
  - Resets CPU, memory, and input queue
  - Sets PC to 0o100 (program entry point)
  
- **Run**: Execute continuously until:
  - Input is required (waits for console input)
  - HLT instruction is encountered
  - Fault condition occurs
  - Smart restart: automatically restarts from 0o100 if halted
  
- **Step**: Execute one instruction and update all displays
  
- **Halt**: Stop program execution immediately

- **Load / Load+ / Store / Store+**: Manual memory operations
  - Use OCTAL INPUT field to specify address
  - Load: Read from MAR into MBR
  - Load+: Read and increment MAR
  - Store: Write MBR to MAR
  - Store+: Write and increment MAR

### Input/Output Areas

- **OCTAL INPUT**: 
  - Type octal values (0-7 digits)
  - Live binary display shows 16-bit representation
  - Used for register loading and memory operations
  
- **Console Input** (bottom center):
  - For Program 2: Enter search word and press Enter or Submit
  - Supports text input (automatically converts to character codes)
  - Supports numeric input (decimal values)
  - Text echoed to printer output

- **Printer Output** (center):
  - Displays program output and system messages
  - Shows paragraph sentences (Program 2)
  - Shows search results with sentence and word position
  - Logs execution events (optional)

## Program Files and Usage

### Program 2: Paragraph Word Search

**Default program**: `simulator/Program2.txt` (automatically preselected by IPL)

**How to use**:
1. Click **IPL** to load Program2.txt
2. Click **Run** to start execution
3. Program displays 6 sentences from the paragraph
4. Program prompts for a word to search
5. Type the search word in **Console Input** and press Enter
6. Program displays results:
   - If found: "Word: [word], Sentence: [N], Position: [M]"
   - If not found: "Word not found in paragraph"

## Troubleshooting

### GUI Issues

- **Nothing happens or GUI closes immediately**:
  - Confirm Java 21: `java -version` or check `$env:JAVA_HOME`
  - Run from PowerShell to see console error messages
  - Check if JAR file exists in `simulator/target/`

- **GUI appears but IPL button doesn't work**:
  - Ensure Program2.txt exists in simulator folder
  - Check file permissions (read access required)

### Execution Issues

- **Program doesn't start after IPL**:
  - Click **Run** button (not automatic)
  - Check PC is set to 0o100 (shown in register panel)
  - Verify no error messages in printer output

- **Execution pauses and nothing happens**:
  - Program is waiting for input
  - Enter text in **Console Input** field
  - Press Enter or Submit button
  - Click **Run** or **Step** to continue

- **Program shows "Waiting for input" repeatedly**:
  - Enter your search word in Console Input
  - Press Enter to submit
  - Each character must be read by the program

### Memory and Address Issues

- **Memory faults (out of bounds)**:
  - Valid addresses: octal `0000`–`3777` (0–2047 decimal)
  - Check your program doesn't access invalid addresses
  - MFR register shows fault code (bit 0x4 for memory fault)

- **Unexpected halt immediately after start**:
  - Load correct program file (Program2.txt)
  - Verify program code at address 0o100
  - Check for divide-by-zero (MFR bit 0x2)
  - Check for illegal opcode (MFR bit 0x1)

### I/O Issues

- **Text input not working**:
  - Make sure Console Input field has focus
  - Press Enter after typing
  - Or click Submit button
  - Check printer output for feedback

- **Search results not appearing**:
  - Ensure word exists in paragraph
  - Check case sensitivity (exact match required)
  - Verify program completed (CPU halted)
  - Review printer output for all messages

### Cache and Performance

- **Cache display not updating**:
  - Click Step to see cache changes per instruction
  - Run mode updates after program completes
  - Check Cache Content panel on right side

- **Program runs too slowly**:
  - Normal: 4ms delay per instruction in Run mode
  - Use Step for instruction-by-instruction debugging
  - Halt and restart if needed

## Architecture Notes

- **Main class**: `edu.gwu.cs6461.Main`
- **Runnable JAR**: `simulator/target/CS6461Simulator-1.0.0.jar`
- **Dependencies**: None (pure Swing application)
- **Java version**: Requires Java 21+


## Additional Resources

- **DESIGN_NOTES.md**: Comprehensive architecture and design documentation
- **Source code**: `src/main/java/edu/gwu/cs6461/`
  - `Main.java`: Entry point
  - `SimulatorGUI.java`: GUI implementation
  - `CPU.java`: Instruction execution engine
  - `Memory.java`: Memory management with cache
  - `Cache.java`: Cache implementation

## Tips for Best Experience

1. **Loading Programs**: Always use IPL button to ensure proper initialization
2. **Input Timing**: Enter input when program prompts (watch printer output)
3. **Debugging**: Use Step button to execute one instruction at a time
4. **Register Loading**: Use individual Load buttons after setting Octal Input
5. **Cache Observation**: Step through program to see cache behavior
6. **Restart**: Use Run button on halted program to restart at 0o100
7. **Text Input**: For word search, type complete word and press Enter once

