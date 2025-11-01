import java.io.*;

public class Loader {
    // Expect lines like: "000144   040422" (octal)
    public static void loadFile(String path, Memory mem, Cache cache, CPU cpu) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            int firstAddr = -1;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\s+");
                if (parts.length < 2) continue;

                int addr = Integer.parseInt(parts[0], 8);
                int word = Integer.parseInt(parts[1], 8) & 0xFFFF;

                cache.write(addr, word); // load via cache (write-allocate)

                if (firstAddr < 0) firstAddr = addr;
            }
            if (firstAddr >= 0) {
                cpu.setPC(firstAddr);
                System.out.printf("Program loaded. PC initialized to %04o\n", firstAddr);
            } else {
                throw new IllegalArgumentException("No loadable lines found.");
            }
        }
    }
}
