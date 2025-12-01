package edu.gwu.cs6461;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimulatorGUI simulator = new SimulatorGUI();
            simulator.setVisible(true);
        });
    }
}