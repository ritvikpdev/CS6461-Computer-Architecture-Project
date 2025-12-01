# CS6461 Basic Machine Simulator — Design Notes

These notes describe the design and working of the Swing-based Basic Machine simulator and its main classes. The simulator executes programs assembled into the octal load format (e.g., `Program2.txt`), presents a GUI for interacting with registers/memory and I/O, and models a small cache in front of main memory.

## Overview

- **Word size**: 16 bits (two's complement)
- **Address size**: 12 bits (0–2047, masked to 11 bits for effective addressing)
- **Memory**: 2048 words (0–2047)
- **Registers**: 
  - GPRs R0–R3 (16-bit general purpose)
  - IX1–IX3 (16-bit index registers)
  - PC (12-bit program counter)
  - MAR (12-bit memory address register)
  - MBR (16-bit memory buffer register)
  - IR (16-bit instruction register)
  - CC (4-bit condition code)
  - MFR (4-bit machine fault register)
- **ISA**: Load/Store, Arithmetic/Logical, Shift/Rotate, Control Flow, I/O. Includes MLT, DVD, TRR, AND, ORR, NOT, shift/rotate operations.
- **I/O**: Console keyboard input (decimal), console printer output, with intelligent queue management.
- **Cache**: 16-line fully associative, FIFO replacement, write-through policy.

## Execution Model

The CPU runs a classic fetch–decode–execute loop:

1. Fetch: `IR <- M[PC]`, `PC <- PC + 1`.
2. Decode: Extract fields: opcode, R/IX/I, and address.
3. Effective address (EA) resolution:
   - Base EA from 11-bit address field.
   - If IX != 0, add IX register.
   - If indirect (I=1), read memory at EA; the loaded pointer is masked to 11 bits.
   - Final EA always masked to 11 bits.
4. Execute: Dispatch by opcode to the instruction implementation.
5. Write-back and update condition codes (when applicable).

The GUI controls single-step/run and exposes register and memory state along with a printer console and cache window.

## Memory and Addressing

- **Address masking**: All memory accesses use 12-bit addresses, masked to 11 bits for effective addressing: `ea = ea & 0x7FF`.
- **Indirect addressing**: Reads a pointer from memory and masks it before use.
- **Fault handling**: Out-of-range addresses set MFR bits and halt execution safely.
- **Memory class**: Encapsulates 2048-word array with integrated cache interface.

## I/O Model

- **Input (Device 0)**: Reads decimal integers from GUI console input queue. Returns -1 if queue empty, causing CPU to retry instruction.
- **Output (Device 1)**: Writes decimal values to GUI printer console, supports both signed and unsigned display.
- **Output (Device 2)**: Raw character output with `[RAW]` prefix handling for text display.
- **Character I/O (Device 3)**: Character-by-character input for text processing and word search operations.
- **Input Queue**: FIFO queue with intelligent management:
  - Accepts text input (character sequences for word search)
  - Accepts bulk input (space/comma-separated)
  - Tracks input consumption per run
  - Automatic mode detection (Program1 vs Program2)
  - Smart wait messages without repetition
  - Supports both numeric and text input with automatic character conversion

## Cache Behavior

- **Architecture**: Fully associative, 16 cache lines
- **Replacement policy**: FIFO (First-In-First-Out)
- **Write policy**: Write-through (updates both cache and memory simultaneously)
- **Cache line structure**: Tag (address), valid bit, data (16-bit word)
- **GUI display**: Formatted cache view showing line number (decimal), tag (octal), and data (octal)
- **Performance**: Provides observability of memory access patterns for educational purposes

## Control Flow and Branch Logging

Conditional and unconditional transfers (JZ, JNE, JCC, JMA, JSR, RFS, SOB, JGE) compute EA as above and may update PC. The simulator logs, for each branch:

- Decoded condition
- Computed EA and whether taken/not taken
- New PC if taken

This aids debugging assembled programs and diagnosing unexpected halts.

## Core Classes

### edu.gwu.cs6461.CPU

- **Core responsibilities**:
  - Maintains all registers (GPRs, IX, PC, IR, MAR, MBR, CC, MFR) as integer values
  - Implements complete instruction set via `decodeAndExecute()`
  - Manages fetch-decode-execute cycle
  - Handles effective address computation with indexing and indirection
  - Manages halt state and execution control

- **Instruction categories**:
  - **Load/Store**: LDR, STR, LDA, LDX, STX
  - **Arithmetic**: AMR, SMR, AIR, SIR, MLT, DVD
  - **Logical**: TRR, AND, ORR, NOT
  - **Shift/Rotate**: SRC, RRC (arithmetic/logical, left/right)
  - **Control flow**: JZ, JNE, JCC, JMA, JSR, RFS, SOB, JGE
  - **I/O**: IN, OUT (devices 0-3)

- **Architecture**:
  - Uses functional programming patterns with `Supplier<Integer>` for input and `Consumer<String>` for output
  - Direct integration with `Memory` class (no utility class dependencies)
  - Manual memory operations: `manual_load()`, `manual_load_plus()`, `manual_store()`, `manual_store_plus()`
  - Thread-safe GUI updates via callback pattern
  - Proper masking of all register values (16-bit for data, 12-bit for addresses, 4-bit for status)

### edu.gwu.cs6461.Cache

- **Structure**: 16 cache lines with tag, valid bit, and data fields
- **CacheLine inner class**: Encapsulates tag (address), valid flag, and 16-bit data
- **Replacement**: FIFO queue tracks line insertion order
- **Read path**: 
  - Search all lines for matching valid tag
  - Return data on hit, null on miss
- **Write path**: 
  - Update existing line if hit
  - Find empty line or FIFO evict on miss
  - Write-through policy ensures memory consistency
- **API**: 
  - `read(address)` returns Short or null
  - `write(address, data)` returns line index
  - `getLines()` exposes cache state for GUI
  - `clear()` invalidates all lines

### edu.gwu.cs6461.Memory

- **Responsibilities**:
  - Manages 2048-word memory array (short[])
  - Integrates cache interface for all memory accesses
  - Loads programs from octal format files
  - Implements bounds checking with exception handling

- **Program loading**:
  - Supports both filesystem and classpath resource loading
  - Parses octal address/value pairs
  - Handles comments and empty lines
  - Reports invalid addresses

- **Cache integration**:
  - `getValueAt()`: Checks cache first, updates on miss
  - `setValueAt()`: Write-through to both cache and memory
  - `getCache()`: Returns cache instance for GUI display

### edu.gwu.cs6461.SimulatorGUI

- **Architecture**: Modern Swing-based GUI with BorderLayout structure and native look-and-feel

- **Layout**:
  - **NORTH**: Toolbar with operation buttons (IPL, Run, Step, Halt, Load, Load+, Store, Store+) and binary/octal display
  - **WEST**: Scrollable register panel with GPRs, IXRs, and internal registers (PC, MAR, MBR, IR, CC, MFR)
  - **CENTER**: Main I/O area with printer output and console input
  - **EAST**: Cache content display

- **Enhanced Features**:
  - **Program mode detection**: Automatically detects Program1 vs Program2 from filename
  - **Smart input queue**: FIFO queue with bulk entry support, mode-specific constraints
  - **Input tracking**: Counts consumed inputs, shows progress messages
  - **Automatic summary**: Extracts and displays search results based on program mode
  - **Printer buffer**: Maintains full output history for result extraction
  - **State management**: Tracks wait states, prevents duplicate messages
  - **Text input support**: Handles word input for Program2 search functionality

- **User interactions**:
  - **IPL**: File chooser with Program2.txt default, automatic mode detection, PC set to 0o100
  - **Run**: Smart restart from halt state, input consumption tracking, automatic result display
  - **Step**: Single instruction execution with immediate display update
  - **Register loading**: Individual Load buttons for each register using octal input
  - **Memory operations**: Manual load/store with address increment support
  - **Console input**: Supports both numeric and text input, automatic character conversion, queue status feedback

- **Display formatting**:
  - All registers shown in octal (6 digits for data, 4 for addresses)
  - CC and MFR shown in binary (4 bits)
  - Cache shows line number (decimal), tag and data (octal)
  - Binary display shows 16-bit representation of octal input

### edu.gwu.cs6461.Main

- **Entry point**: Uses `SwingUtilities.invokeLater()` for thread-safe GUI initialization
- **Responsibilities**: Creates SimulatorGUI instance and makes it visible
- **Thread safety**: Ensures GUI components created on Event Dispatch Thread

## Program Loading and Format

- **File format**: Octal load files with `address value` pairs (space-separated)
- **Comment support**: Lines starting with `#` are ignored
- **Empty lines**: Skipped during parsing
- **Default location**: `simulator/Program2.txt` for demos
- **IPL process**:
  1. Reset memory and CPU state
  2. Load program from file into memory
  3. Set PC to 0o100 (decimal 64) - program entry point
  4. Clear console input queue
  5. Reset tracking variables (input count, wait state, summary flag)
  6. Display ready message with mode-specific instructions
  7. Detect program mode from filename for appropriate I/O behavior

## Logging, Errors, and Faults

- **Console output**: All program output and system messages appear in printer area
- **Fault handling**: 
  - Memory bounds violations set MFR bit 0x4 and halt
  - Divide by zero sets MFR bit 0x2 and halts
  - Illegal opcodes set MFR bit 0x1 and halt
- **Diagnostic messages**: 
  - Opcode execution logging to System.out
  - Input queue status updates
  - Program load confirmation
  - IPL and restart notifications
- **Error dialogs**: JOptionPane alerts for:
  - Invalid octal input
  - File loading errors
  - Invalid start addresses

## Extensibility

- **New instructions**: 
  - Add case to CPU's `decodeAndExecute()` switch statement
  - Implement instruction logic following existing patterns
  - Update documentation

- **I/O devices**: 
  - Add device ID to IN/OUT case statements
  - Wire new supplier/consumer to CPU
  - Update GUI for new I/O controls if needed

- **Cache policies**: 
  - Modify Cache class methods
  - Update FIFO queue management
  - Alternative associativities via line selection logic

- **GUI enhancements**: 
  - Modular panel structure allows easy additions
  - BorderLayout supports new regions
  - Toolbar buttons follow consistent styling pattern

## Build and Run

- Maven build creates a runnable JAR with `edu.gwu.cs6461.Main` as the entry point.
- See `simulator/README.md` for step-by-step build/run instructions and troubleshooting.

## Known Limitations and Notes

- **Timing**: No cycle-accurate timing; execution speed controlled by 4ms delay in run loop
- **I/O simulation**: GUI-based I/O without hardware latency modeling
- **Cache scope**: Educational implementation; write-through policy for simplicity
- **Address space**: 12-bit addresses masked to 11 bits (0-2047) for effective addressing
- **Number format**: 
  - Memory files use octal format
  - Console input accepts decimal for user convenience
  - Console output displays decimal for readability
- **Thread safety**: All GUI updates use SwingUtilities.invokeLater() or Platform checks
- **Program-specific features**: 
  - Input constraints adapt based on detected program mode
  - Automatic result extraction varies by program type
  - Program2 mode supports text-based input/output

## Program 2: Paragraph Word Search

The simulator is designed to support Program 2, which performs word search in a paragraph:

### Program 2 Functionality

**Task**: Read a paragraph of 6 sentences from a file, print the sentences, accept a search word from the user, and locate the word within the paragraph.

**Input/Output Flow**:
1. Program reads paragraph from memory (loaded at address 0o1750 / decimal 1000)
2. Displays 6 sentences on console printer
3. Prompts user for a word to search
4. User enters word via console input
5. Program searches paragraph for the word
6. If found: displays word, sentence number, and word position within sentence
7. If not found: displays nothing and exit with opcode 0

### Simulator Support for Program 2

1. **Text Input Handling**:
   - Console input accepts text strings for word search
   - Automatic character-by-character conversion to memory format
   - Null terminator (0) added automatically
   - No artificial input limits in Program2 mode

2. **Character I/O**:
   - Device 3 (IN) reads individual characters for word input
   - Device 2 (OUT) writes formatted text output with `[RAW]` prefix
   - Supports sentence-by-sentence display

3. **Memory Layout**:
   - Paragraph stored starting at 0o1750 (decimal 1000)
   - Each character stored as 16-bit word (low byte contains ASCII)
   - Sentences separated by periods or newlines
   - Null terminators mark end of strings

4. **Search Operations**:
   - Program iterates through paragraph character-by-character
   - Tracks sentence boundaries (period detection)
   - Counts word positions within each sentence
   - Case-sensitive or case-insensitive matching (program-dependent)

5. **Result Display**:
   - Found: "Word: [word], Sentence: [N], Position: [M]"
   - Not found: "Word not found in paragraph"
   - All output appears in console printer area

### Program Mode Detection

The simulator automatically detects the program type:
- **Filename check**: "Program1" or "program2" in filename → Program2 mode
- **Behavior changes**: 
  - Removes 21-value input limit
  - Disables automatic numeric result extraction
  - Enables text input processing
  - Adjusts wait messages for text input
