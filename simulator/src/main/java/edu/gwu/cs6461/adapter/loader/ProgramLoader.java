package edu.gwu.cs6461.adapter.loader;

import java.io.IOException;
import edu.gwu.cs6461.core.Memory;

public interface ProgramLoader {
    void loadProgram(Memory memory) throws IOException;
    default int programStartAddress(){ return 0; }
}
