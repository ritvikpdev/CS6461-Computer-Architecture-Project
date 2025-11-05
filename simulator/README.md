# CS6461 Basic Machine Simulator (Swing)

A Swing-based simulator for the CS6461 Basic Machine. It provides a GUI to view registers and cache, load and execute programs, and interact via console input/output.

## Requirements

- Java 21 (e.g., Eclipse Adoptium JDK 21)
- Maven 3.8+
- Windows PowerShell commands are shown below; adjust for your shell if different.

## Build

From the simulator module folder (relative to the project root):

```powershell
cd .\simulator
mvn clean package -DskipTests
```

This produces a runnable JAR at:

```
simulator\target\CS6461Simulator-1.0.0.jar
```

## Run

Using JAVA_HOME (recommended):

```powershell
& "$env:JAVA_HOME\bin\java.exe" -jar ".\simulator\target\CS6461Simulator-1.0.0.jar"
```

If `java` is on PATH:

```powershell
java -jar ".\simulator\target\CS6461Simulator-1.0.0.jar"
```

### Run from project root

```powershell
# Build
cd .\simulator; mvn clean package -DskipTests

# Run (JAVA_HOME)
& "$env:JAVA_HOME\bin\java.exe" -jar ".\simulator\target\CS6461Simulator-1.0.0.jar"

# Or one-liner
pushd .\simulator; mvn clean package -DskipTests; & "$env:JAVA_HOME\bin\java.exe" -jar ".\target\CS6461Simulator-1.0.0.jar"; popd
```

## Using the GUI

- Buttons (top toolbar):
  - **IPL**: Initialize and load a program. The file chooser opens in the simulator folder and preselects `Program1.txt`.
  - **Run**: Execute continuously until input is required or a HLT is encountered.
  - **Step**: Execute one instruction.
  - **Halt**: Stop execution.
  - **Load / Load+ / Store / Store+**: Use the OCTAL INPUT field to provide values; operates on MAR/MBR based on the selected operation.
- **OCTAL INPUT**: Type octal values; the binary display updates live.
- **Printer** (center): Shows instruction trace, cache hits/misses, effective address resolution, and branch decisions.
- **Console Input**: Enter input text and press Enter or the Submit button; input is fed to device 0 (keyboard). Echoed to the Printer.
- **Cache Content** (right): Displays the current state of a 16-line fully associative cache with FIFO replacement.

## Program files

- Default program: `simulator/Program1.txt` (preselected by IPL). This file mirrors the current assembler output and avoids unintended indirect jumps.
- You may also load `assembler/output/loadfile.txt` directly—it contains the same assembled words.

Important: If you observe a jump to address `002770` followed by immediate HLT, ensure your program file has at address `000032` the word `037410` (JGE, direct). If it shows `037450` (JGE, indirect), you loaded a stale file.

## Troubleshooting

- Nothing happens or GUI closes immediately:
  - Confirm Java 21: `java -version` or use `$env:JAVA_HOME`.
  - Run from PowerShell to see console messages.
- Halts at `002770` right after starting:
  - Load `simulator/Program1.txt` (preselected by IPL) or the assembler `output/loadfile.txt`.
  - Verify `000032` is `037410` (not `037450`).
- Memory faults (out of bounds):
  - Valid addresses are octal `0000`–`3777` (0–2047 decimal). Reserved `000000`–`000005` are not used by programs.
- Input appears to pause Run:
  - The simulator waits on IN; provide input in the Console Input field and press Enter/Submit, then press Run/Step to continue.

## Packaging details

- Main class: `edu.gwu.cs6461.Main`
- Runnable JAR: `simulator/target/CS6461Simulator-1.0.0.jar`
- No external dependencies required (pure Swing app).

## Notes

- The toolbar highlights **IPL** and **Halt** with a subtle light shade for visibility.
- Branch logging for JZ/JNE/JCC/JMA/JSR/RFS/SOB/JGE clearly indicates whether branches are taken and the target PC.
- Indirect addressing final effective addresses are masked to 11 bits (memory size).

