package src;

import java.util.HashMap;

/**
 * SymbolTable: stores label → address mapping.
 */
public class SymbolTable {
    private HashMap<String, Integer> table = new HashMap<>();

    public void add(String label, int address) throws Exception {
        String key = label.toUpperCase();
        if (table.containsKey(key)) throw new Exception("Duplicate label: " + label);
        table.put(key, address);
    }

    public int get(String label) {
        return table.getOrDefault(label.toUpperCase(), -1);
    }

    public boolean contains(String label) {
        return table.containsKey(label.toUpperCase());
    }
}
