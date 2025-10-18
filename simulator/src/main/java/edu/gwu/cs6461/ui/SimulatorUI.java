package edu.gwu.cs6461.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SimulatorUI extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/ui/SimulatorView.fxml")));
        stage.setTitle("CS6461 Basic Machine Simulator");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args){ launch(args); }
}
