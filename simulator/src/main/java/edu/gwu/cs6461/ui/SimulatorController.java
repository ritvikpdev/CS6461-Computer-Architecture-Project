package edu.gwu.cs6461.ui;

import java.io.File;

import edu.gwu.cs6461.adapter.ROMLoader;
import edu.gwu.cs6461.core.CPU;
import edu.gwu.cs6461.core.Memory;
import edu.gwu.cs6461.core.Registers;
import edu.gwu.cs6461.core.util.MachineFaultException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

public class SimulatorController {

    // Backend
    private final Registers regs = new Registers();
    private final Memory mem = new Memory(2048);
    private final CPU cpu = new CPU(regs, mem);

    // UI fields
    @FXML private TextField r0, r1, r2, r3, x1, x2, x3, pc, ir, mar, mbr, cc;
    @FXML private TextArea memoryArea;
    @FXML private Button btnIPL, btnStep, btnRun, btnReset;
    @FXML private Label status;

    @FXML
    private void initialize(){
        refresh();
    }

    @FXML
    private void onIPL(){
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Open Load File");
            File f = fc.showOpenDialog(btnIPL.getScene().getWindow());
            if (f == null) return;
            new ROMLoader().load(f, mem, regs);
            status.setText("Loaded: " + f.getName() + "  PC=" + regs.getPC());
            refresh();
        } catch (Exception ex){
            showError(ex);
        }
    }

    @FXML
    private void onStep(){
        try {
            cpu.step();
            refresh();
        } catch (Exception ex){
            showError(ex);
        }
    }

    @FXML
    private void onRun(){
        try {
            cpu.run(10_000); // safety cap
            refresh();
        } catch (Exception ex){
            showError(ex);
        }
    }

    @FXML
    private void onReset(){
        cpu.reset();
        mem.reset();
        status.setText("Reset complete.");
        refresh();
    }

    private void refresh(){
        r0.setText(v(regs.getR(0))); r1.setText(v(regs.getR(1))); r2.setText(v(regs.getR(2))); r3.setText(v(regs.getR(3)));
        x1.setText(v(regs.getX(1))); x2.setText(v(regs.getX(2))); x3.setText(v(regs.getX(3)));
        pc.setText(v(regs.getPC())); ir.setText(v(regs.getIR())); mar.setText(v(regs.getMAR())); mbr.setText(v(regs.getMBR()));
        cc.setText(String.format("0x%X", regs.getCC()));

        StringBuilder sb = new StringBuilder();
        for (int a=0; a<128; a++){
            sb.append(String.format("%04d : %04X%n", a, safeRead(a)));
        }
        memoryArea.setText(sb.toString());
    }

    private String v(int n){ return String.valueOf(n & 0xFFFF); }

    private int safeRead(int a){
        try { return mem.read(a); } catch (MachineFaultException e){ return 0; }
    }

    private void showError(Exception ex){
        status.setText("FAULT: " + ex.getMessage());
        new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
    }
}
