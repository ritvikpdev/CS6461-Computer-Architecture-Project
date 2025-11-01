import java.awt.*;
import javax.swing.*;

public class ConsoleUI extends JFrame {
    private final CPU cpu;
    private final Memory mem;
    private final Cache cache;

    private JTextField[] gprField = new JTextField[4];
    private JTextField pcField, marField, mbrField, irField, fileField;

    // cache stats
    private JTextField hitsField, missesField, rateField;

    public ConsoleUI(CPU cpu, Memory mem, Cache cache) {
        this.cpu = cpu;
        this.mem = mem;
        this.cache = cache;
        setTitle("C6461 Computer Simulator (Part 2: Cache, 8-line)");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,5,5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.WEST;

        int row = 0;

        // GPRs
        for (int i = 0; i < 4; i++) {
            c.gridy = row;
            c.gridx = 0; panel.add(new JLabel("GPR" + i), c);
            c.gridx = 1; gprField[i] = new JTextField("000000", 10); panel.add(gprField[i], c);
            c.gridx = 2;
            JButton apply = new JButton("Apply");
            final int r = i;
            apply.addActionListener(e -> {
                try {
                    int val = Integer.parseInt(gprField[r].getText(), 8);
                    cpu.setGPR(r, val);
                    refreshDisplay();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid octal for GPR" + r);
                }
            });
            panel.add(apply, c);
            row++;
        }

        // Registers
        pcField  = addRegister(panel, "PC",  row++);
        marField = addRegister(panel, "MAR", row++);
        mbrField = addRegister(panel, "MBR", row++);
        irField  = addRegister(panel, "IR",  row++);

        // Cache stats
        c.gridy = row++; c.gridx = 0; panel.add(new JLabel("Cache Hits"), c);
        c.gridx = 1; hitsField = new JTextField("0", 10); hitsField.setEditable(false); panel.add(hitsField, c);

        c.gridy = row++; c.gridx = 0; panel.add(new JLabel("Cache Misses"), c);
        c.gridx = 1; missesField = new JTextField("0", 10); missesField.setEditable(false); panel.add(missesField, c);

        c.gridy = row++; c.gridx = 0; panel.add(new JLabel("Hit Rate"), c);
        c.gridx = 1; rateField = new JTextField("0.000", 10); rateField.setEditable(false); panel.add(rateField, c);

        // Cache control
        c.gridy = row; c.gridx = 0;
        JButton clearCacheBtn = new JButton("Clear Cache");
        clearCacheBtn.addActionListener(e -> {
            cache.clear();
            refreshDisplay();
            JOptionPane.showMessageDialog(this, "Cache cleared.");
        });
        panel.add(clearCacheBtn, c);

        // File controls
        row++;
        c.gridy = row; c.gridx = 0; panel.add(new JLabel("Program File:"), c);
        c.gridx = 1; fileField = new JTextField(30); panel.add(fileField, c);
        c.gridx = 2; JButton browseBtn = new JButton("Browse");
        browseBtn.addActionListener(e -> browseFile());
        panel.add(browseBtn, c);
        c.gridx = 3; JButton loadBtn = new JButton("Load File");
        loadBtn.addActionListener(e -> loadProgram());
        panel.add(loadBtn, c);

        // Control buttons
        row++;
        c.gridy = row; c.gridx = 0;
        JButton runBtn = new JButton("Run");
        runBtn.addActionListener(e -> new Thread(cpu::run).start()); // background thread
        panel.add(runBtn, c);

        c.gridx = 1;
        JButton stepBtn = new JButton("Step");
        stepBtn.addActionListener(e -> cpu.step());
        panel.add(stepBtn, c);

        add(new JScrollPane(panel));
    }

    private JTextField addRegister(JPanel p, String name, int row) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,5,5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = row;
        c.gridx = 0; p.add(new JLabel(name), c);
        c.gridx = 1; JTextField t = new JTextField("000000", 10);
        t.setEditable(false);
        p.add(t, c);
        return t;
    }

    private void browseFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void loadProgram() {
        try {
            Loader.loadFile(fileField.getText(), mem, cache, cpu);
            refreshDisplay();
            JOptionPane.showMessageDialog(this, "Program loaded successfully! PC set automatically.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage());
        }
    }

    /** Refresh GUI from CPU + cache */
    public void refreshDisplay() {
        for (int i = 0; i < 4; i++) gprField[i].setText(String.format("%06o", cpu.getGPR(i)));
        pcField.setText(String.format("%06o", cpu.getPC()));
        marField.setText(String.format("%06o", cpu.getMAR()));
        mbrField.setText(String.format("%06o", cpu.getMBR()));
        irField.setText(String.format("%06o", cpu.getIR()));

        hitsField.setText(Integer.toString(cache.getHits()));
        missesField.setText(Integer.toString(cache.getMisses()));
        rateField.setText(String.format("%.3f", cache.getHitRate()));
    }
}
