package edu.gwu.cs6461;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.awt.event.ActionListener;

/**
 * The main Graphical User Interface for the CSCI 6461 Machine Simulator.
 */
public class SimulatorGUI extends JFrame {

    // === Theme Constants ===
    private static final Color COLOR_BACKGROUND = UIManager.getColor("Panel.background");
    private static final Color COLOR_FOREGROUND = UIManager.getColor("TextField.foreground");
    private static final Font FONT_MONO = new Font("Monospaced", Font.PLAIN, 12);
    private static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD, 12);
    private static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD, 12);
    // ============================

    private final CPU cpu;

    // GUI Components
    private final JTextField[] gprTextFields = new JTextField[4];
    private final JTextField[] ixrTextFields = new JTextField[3];
    private final JTextField pcTextField = new JTextField(6); // 4 octal digits
    private final JTextField marTextField = new JTextField(6); // 4 octal digits
    private final JTextField mbrTextField = new JTextField(8); // 6 octal digits
    private final JTextField irTextField = new JTextField(8); // 6 octal digits
    private final JTextField ccTextField = new JTextField(6); // 4 binary digits
    private final JTextField mfrTextField = new JTextField(6); // 4 binary digits
    private final JTextField binaryDisplayField = new JTextField(16);
    private final JTextField octalInputField = new JTextField(8);
    private final JTextArea cacheContentArea = new JTextArea(20, 35);
    private final JTextArea printerArea = new JTextArea(20, 60);
    private final JTextField consoleInputTextField = new JTextField(60);

    public SimulatorGUI() {
        this.cpu = new CPU(this);

        // --- NEW: Set Native Look and Feel ---
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Warning: Could not set system look and feel.");
        }
        // -------------------------------------

        setTitle("CSCI 6461 Machine Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true); // Allow resizing
        setLayout(new BorderLayout(5, 5)); // Use BorderLayout
        getContentPane().setBackground(COLOR_BACKGROUND);

        setupComponents();
        addListeners();

        setMinimumSize(new Dimension(1024, 768)); // Set a reasonable minimum size
        pack();
        setLocationRelativeTo(null);
        cpu.resetMachine(); // Perform initial reset on startup
        updateAllDisplays();
        cacheContentArea.setText(""); // Clear cache content on startup
    }

    private void setupComponents() {
        // --- REFACTORED: New BorderLayout Layout ---
        
        // NORTH: Operation Buttons
        add(createOperationButtonsPanel(), BorderLayout.NORTH);

        // WEST: All Registers
        JScrollPane registerScrollPane = new JScrollPane(createRegisterPanel());
        registerScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        registerScrollPane.setBorder(null);
        add(registerScrollPane, BorderLayout.WEST);

        // CENTER: Main I/O (Printer and Console)
        add(createCenterPanel(), BorderLayout.CENTER);

        // EAST: Cache Content
        add(createCachePanel(), BorderLayout.EAST);
    }

    /**
     * Creates the left-side panel containing all registers (GPR, IXR, Internal).
     */
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(createGPRPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createIXRPanel());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createInternalRegisterPanel());
        panel.add(Box.createVerticalGlue()); // Pushes everything up

        return panel;
    }

    /**
     * Creates the GPR sub-panel for the left register panel.
     */
    private JPanel createGPRPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BACKGROUND);
        TitledBorder border = BorderFactory.createTitledBorder("General Purpose Registers");
        border.setTitleColor(COLOR_FOREGROUND);
        panel.setBorder(border);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        for (int i = 0; i < 4; i++) {
            gbc.gridy = i;
            
            JLabel label = createStyledLabel("GPR " + i);
            gbc.gridx = 0;
            gbc.weightx = 0.1;
            panel.add(label, gbc);
            
            gprTextFields[i] = createRegisterTextField(8);
            gbc.gridx = 1;
            gbc.weightx = 0.8;
            panel.add(gprTextFields[i], gbc);
            
            int index = i; // final for lambda
            JButton button = createLoadButton(e -> loadRegisterValue("GPR", index));
            gbc.gridx = 2;
            gbc.weightx = 0.1;
            panel.add(button, gbc);
        }
        return panel;
    }

    /**
     * Creates the IXR sub-panel for the left register panel.
     */
    private JPanel createIXRPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BACKGROUND);
        TitledBorder border = BorderFactory.createTitledBorder("Index Registers");
        border.setTitleColor(COLOR_FOREGROUND);
        panel.setBorder(border);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < 3; i++) {
            gbc.gridy = i;
            
            JLabel label = createStyledLabel("IXR " + (i + 1));
            gbc.gridx = 0;
            gbc.weightx = 0.1;
            panel.add(label, gbc);
            
            ixrTextFields[i] = createRegisterTextField(8);
            gbc.gridx = 1;
            gbc.weightx = 0.8;
            panel.add(ixrTextFields[i], gbc);
            
            int index = i; // final for lambda
            JButton button = createLoadButton(e -> loadRegisterValue("IXR", index));
            gbc.gridx = 2;
            gbc.weightx = 0.1;
            panel.add(button, gbc);
        }
        return panel;
    }

    /**
     * Creates the Internal Registers sub-panel for the left register panel.
     */
    private JPanel createInternalRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BACKGROUND);
        TitledBorder border = BorderFactory.createTitledBorder("Internal Registers");
        border.setTitleColor(COLOR_FOREGROUND);
        panel.setBorder(border);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // PC
        gbc.gridy = 0;
        gbc.gridx = 0; panel.add(createStyledLabel("PC"), gbc);
        gbc.gridx = 1; panel.add(pcTextField, gbc);
        gbc.gridx = 2; panel.add(createLoadButton(e -> loadRegisterValue("PC", 0)), gbc);
        
        // MAR
        gbc.gridy = 1;
        gbc.gridx = 0; panel.add(createStyledLabel("MAR"), gbc);
        gbc.gridx = 1; panel.add(marTextField, gbc);
        gbc.gridx = 2; panel.add(createLoadButton(e -> loadRegisterValue("MAR", 0)), gbc);
        
        // MBR
        gbc.gridy = 2;
        gbc.gridx = 0; panel.add(createStyledLabel("MBR"), gbc);
        gbc.gridx = 1; panel.add(mbrTextField, gbc);
        gbc.gridx = 2; panel.add(createLoadButton(e -> loadRegisterValue("MBR", 0)), gbc);
        
        // IR
        gbc.gridy = 3;
        gbc.gridx = 0; panel.add(createStyledLabel("IR"), gbc);
        gbc.gridx = 1; panel.add(irTextField, gbc);
        gbc.gridx = 2; panel.add(createDisabledLoadButton(), gbc); // IR not loadable

        // CC
        gbc.gridy = 4;
        gbc.gridx = 0; panel.add(createStyledLabel("CC"), gbc);
        gbc.gridx = 1; panel.add(ccTextField, gbc);
        gbc.gridx = 2; panel.add(createDisabledLoadButton(), gbc); // CC not loadable

        // MFR
        gbc.gridy = 5;
        gbc.gridx = 0; panel.add(createStyledLabel("MFR"), gbc);
        gbc.gridx = 1; panel.add(mfrTextField, gbc);
        gbc.gridx = 2; panel.add(createDisabledLoadButton(), gbc); // MFR not loadable

        // Style text fields
        styleTextField(pcTextField, false);
        styleTextField(marTextField, false);
        styleTextField(mbrTextField, false);
        styleTextField(irTextField, false);
        styleTextField(ccTextField, false);
        styleTextField(mfrTextField, false);

        return panel;
    }

    /**
     * Creates the TOP panel with all operation buttons.
     */
    private JPanel createOperationButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(new Color(230, 230, 230)); // Light gray toolbar background
        panel.setBorder(BorderFactory.createEtchedBorder());
        
        // Create and style each button
        JButton[] buttons = {
            createOperationButton("IPL", true),
            createOperationButton("Run"),
            createOperationButton("Step"),
            createOperationButton("Halt", true),
            createOperationButton("Load"),
            createOperationButton("Load+"),
            createOperationButton("Store"),
            createOperationButton("Store+")
        };
        
        for (JButton button : buttons) {
            panel.add(button);
        }
        
        // Add the Binary/Octal panel to the toolbar
        panel.add(Box.createHorizontalStrut(20));
        panel.add(createBinaryOctalPanel());
        
        return panel;
    }
    
    /**
     * Creates the Binary/Octal input panel (now part of the top toolbar).
     */
    private JPanel createBinaryOctalPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false); // Make it transparent

        panel.add(createStyledLabel("OCTAL INPUT:"));
        styleTextField(octalInputField, true); // Use system text field style
        panel.add(octalInputField);
        
        panel.add(Box.createHorizontalStrut(10));
        
        panel.add(createStyledLabel("BINARY:"));
        styleTextField(binaryDisplayField, true); // Use system text field style
        binaryDisplayField.setEditable(false);
        panel.add(binaryDisplayField);

        return panel;
    }

    /**
     * Creates the main CENTER panel for I/O (Printer and Console).
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(COLOR_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5)); // Top padding removed
        
        // Printer Area
    printerArea.setEditable(false);
    printerArea.setFont(FONT_MONO);
    styleTextField(printerArea, false); // Use monospaced style for consistency
        JScrollPane printerScrollPane = new JScrollPane(printerArea);
        TitledBorder printerBorder = BorderFactory.createTitledBorder("Printer");
        printerBorder.setTitleColor(COLOR_FOREGROUND);
        printerScrollPane.setBorder(printerBorder);
        
        // Console Input
        styleTextField(consoleInputTextField, true); // Use system style for input
        TitledBorder inputBorder = BorderFactory.createTitledBorder("Console Input (Press Enter to Submit)");
        inputBorder.setTitleColor(COLOR_FOREGROUND);
        
        JPanel consolePanel = new JPanel(new BorderLayout(5, 0));
        consolePanel.setBackground(COLOR_BACKGROUND);
        JButton submitButton = createOperationButton("Submit"); // Use styled button
        
        ActionListener submitAction = e -> submitConsoleText();
        submitButton.addActionListener(submitAction);
        consoleInputTextField.addActionListener(submitAction); // This handles Enter key

        consolePanel.add(consoleInputTextField, BorderLayout.CENTER);
        consolePanel.add(submitButton, BorderLayout.EAST);
        consolePanel.setBorder(inputBorder);
        
        panel.add(printerScrollPane, BorderLayout.CENTER);
        panel.add(consolePanel, BorderLayout.SOUTH);
        
        return panel;
    }

    /**
     * Creates the EAST panel for Cache Content.
     */
    private JPanel createCachePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 10)); // Top padding removed
        
    cacheContentArea.setFont(FONT_MONO);
    cacheContentArea.setEditable(false);
    styleTextField(cacheContentArea, false); // Use monospaced style
        JScrollPane cacheScrollPane = new JScrollPane(cacheContentArea);
        TitledBorder cacheBorder = BorderFactory.createTitledBorder("Cache Content");
        cacheBorder.setTitleColor(COLOR_FOREGROUND);
        cacheScrollPane.setBorder(cacheBorder);
        
        panel.add(cacheScrollPane, BorderLayout.CENTER);
        return panel;
    }

    // === Helper methods for creating styled components ===

    private JTextField createRegisterTextField(int columns) {
        JTextField field = new JTextField(columns);
        styleTextField(field, false); // Use dark monospaced style
        field.setEditable(false);
        return field;
    }

    private void styleTextField(JComponent field, boolean useSystemStyle) {
        if (useSystemStyle) {
            // Use standard system text field
            field.setFont(new Font("SansSerif", Font.PLAIN, 12));
            field.setBackground(UIManager.getColor("TextField.background"));
            field.setForeground(UIManager.getColor("TextField.foreground"));
            field.setBorder(UIManager.getBorder("TextField.border"));
            if (field instanceof JTextArea) {
                ((JTextArea)field).setCaretColor(UIManager.getColor("TextField.caretForeground"));
            } else if (field instanceof JTextField) {
                ((JTextField)field).setCaretColor(UIManager.getColor("TextField.caretForeground"));
            }
        } else {
            // Use custom monospaced "register" style
            field.setFont(FONT_MONO);
            field.setBackground(Color.WHITE); // Clean white background
            field.setForeground(Color.BLACK); // Black text
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
            ));
            if (field instanceof JTextArea) {
                ((JTextArea)field).setCaretColor(Color.BLACK);
            } else if (field instanceof JTextField) {
                ((JTextField)field).setHorizontalAlignment(JTextField.CENTER);
                ((JTextField)field).setCaretColor(Color.BLACK);
            }
        }
    }
    
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(COLOR_FOREGROUND);
        return label;
    }
    
    private JButton createOperationButton(String text, boolean... isSpecial) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setFocusPainted(false);

        // Default to Look & Feel colors
        Color defaultBg = UIManager.getColor("Button.background");
        Color defaultFg = UIManager.getColor("Button.foreground");

        if (isSpecial.length > 0 && isSpecial[0]) {
            // Light highlight for special buttons (IPL, Halt)
            Color highlightBg = new Color(230, 240, 255); // subtle light blue
            Color highlightBorder = new Color(180, 200, 230);
            button.setBackground(highlightBg);
            button.setForeground(defaultFg);
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(highlightBorder),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
            ));
        } else {
            // Standard Look & Feel
            button.setBackground(defaultBg);
            button.setForeground(defaultFg);
        }

        button.setPreferredSize(new Dimension(80, 30));
        button.addActionListener(e -> handleButtonPress(text));
        return button;
    }

    private JButton createLoadButton(ActionListener listener) {
        JButton button = new JButton("Load");
        button.setFont(new Font("SansSerif", Font.BOLD, 10));
        button.setMargin(new Insets(2, 5, 2, 5));
        button.setToolTipText("Load value from Octal Input");
        button.addActionListener(listener);
        return button;
    }

    private JButton createDisabledLoadButton() {
        JButton button = new JButton("Load");
        button.setFont(new Font("SansSerif", Font.BOLD, 10));
        button.setMargin(new Insets(2, 5, 2, 5));
        button.setEnabled(false);
        return button;
    }

    // === Core Logic (Listeners, Handlers, Updaters) ===
    // (This section is identical to the previous file and remains unchanged)

    public void appendToPrinter(String text) {
        SwingUtilities.invokeLater(() -> {
            try {
                int len = printerArea.getDocument().getLength();
                if (len > 0) {
                    String last = printerArea.getText(len - 1, 1);
                    if (!"\n".equals(last)) {
                        printerArea.append("\n");
                    }
                }
            } catch (Exception ignored) { /* safe to ignore */ }
            printerArea.append(text + "\n");
            printerArea.setCaretPosition(printerArea.getDocument().getLength());
        });
    }
    
    public void clearPrinter() {
        SwingUtilities.invokeLater(() -> printerArea.setText(""));
    }

    public void printToConsole(String text) {
        if (text == null) return;
        SwingUtilities.invokeLater(() -> {
            printerArea.append(text);
            printerArea.setCaretPosition(printerArea.getDocument().getLength());
        });
    }

    private void addListeners() {
        octalInputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateBinaryDisplayFromOctalInput();
            }
        });
        
        // Console input listener is now an ActionListener, set in createCenterPanel()
    }

    private void submitConsoleText() {
        String text = consoleInputTextField.getText();
        if (text != null && !text.trim().isEmpty()) {
            cpu.submitConsoleInput(text + " ");
            consoleInputTextField.setText("");
            appendToPrinter("-> " + text); // Echo submitted text
        }
    }

    private void updateBinaryDisplayFromOctalInput() {
        try {
            short value = cpu.getUtils().octalToShort(octalInputField.getText());
            binaryDisplayField.setText(cpu.getUtils().shortToBinary(value, 16));
        } catch (NumberFormatException ex) {
            binaryDisplayField.setText("Invalid Octal Input");
        }
    }

    public void handleButtonPress(String command) {
        try {
            System.out.println("\n=== Button Press: " + command + " ===");
            
            short octalValue = 0;
            if (command.equals("Load") || command.equals("Load+") || command.equals("Store") || command.equals("Store+")) {
                 octalValue = cpu.getUtils().octalToShort(octalInputField.getText());
                 System.out.printf("Octal Input Value: %06o\n", octalValue);
            }
            
            switch (command) {
                case "Load" -> cpu.load(octalValue);
                case "Load+" -> cpu.loadPlus(octalValue);
                case "Store" -> cpu.store(octalValue);
                case "Store+" -> cpu.storePlus(octalValue);
                case "Run" -> cpu.runProgram();
                case "Step" -> cpu.singleStep();
                case "Halt" -> cpu.halt();
                case "IPL" -> loadProgramFromFile();
            }
        } catch (NumberFormatException ex) {
            System.out.println("ERROR: Invalid octal input - " + octalInputField.getText());
            showError("Invalid Octal Input", "Please enter a valid octal string (0-7, up to 6 digits).");
        }
    }

    private void loadProgramFromFile() {
        // Prefer starting in the simulator folder and preselect the default Program1.txt
        File cwd = new File(System.getProperty("user.dir"));
        File simDir = new File(cwd, "CS6461-Computer-Architecture-Project" + File.separator + "simulator");
        File defaultProgram = new File(simDir, "Program1.txt");

        JFileChooser fileChooser = simDir.exists() ? new JFileChooser(simDir) : new JFileChooser(cwd);
        if (defaultProgram.exists()) {
            fileChooser.setSelectedFile(defaultProgram);
        }
        fileChooser.setDialogTitle("Select Program File for IPL");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File programFile = fileChooser.getSelectedFile();
            cpu.ipl(programFile);
        }
    }
    
    public void updateAllDisplays() {
        updateRegisters();
        updateMemoryView();
    }

    public void updateRegisters() {
        for (int i = 0; i < 4; i++) gprTextFields[i].setText(cpu.getUtils().shortToOctal(cpu.getGPR(i), 6));
        for (int i = 0; i < 3; i++) ixrTextFields[i].setText(cpu.getUtils().shortToOctal(cpu.getIXR(i + 1), 6));
        pcTextField.setText(cpu.getUtils().shortToOctal(cpu.getPC(), 4));
        marTextField.setText(cpu.getUtils().shortToOctal(cpu.getMAR(), 4));
        mbrTextField.setText(cpu.getUtils().shortToOctal(cpu.getMBR(), 6));
        irTextField.setText(cpu.getUtils().shortToOctal(cpu.getIR(), 6));
        ccTextField.setText(cpu.getUtils().shortToBinary(cpu.getCC(), 4));
        mfrTextField.setText(cpu.getUtils().shortToBinary(cpu.getMFR(), 4));
    }

    public void updateMemoryView() {
        cacheContentArea.setText(cpu.getFormattedCache());
        cacheContentArea.setCaretPosition(0);
    }

    public void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void loadRegisterValue(String registerName, int index) {
        try {
            String octalString = octalInputField.getText();
            if (octalString == null || octalString.trim().isEmpty()) {
                showError("Input Error", "Octal Input field cannot be empty.");
                return;
            }
            short value = cpu.getUtils().octalToShort(octalString);

            System.out.printf("Loading value %06o into %s%s\n", value, registerName, (registerName.equals("IXR") ? index+1 : (registerName.equals("GPR") ? index : "")));

            switch (registerName) {
                case "GPR" -> cpu.setGPR(index, value);
                case "IXR" -> cpu.setIXR(index + 1, value); // IXRs are 1-based in CPU
                case "PC" -> cpu.setPC(value);
                case "MAR" -> cpu.setMAR(value);
                case "MBR" -> cpu.setMBR(value);
                case "IR" -> cpu.setIR(value);
            }

            updateAllDisplays();

        } catch (NumberFormatException ex) {
            showError("Invalid Octal Input", "Please enter a valid octal string.");
        }
    }

}

