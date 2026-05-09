package remoteagent.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.application.Platform; // Add this import

public class WindowController {

    @FXML
    private TextArea logArea;

    @FXML
    public void initialize() {
        System.out.println("Controller loaded!");
        logArea.setText("System ready and Controller linked!");
    }

    public void writeToLog(String message) {
        // Platform.runLater ensures this works even if called from a background thread
        Platform.runLater(() -> {
            logArea.appendText(message + "\n");
        });
    }

    // THIS IS THE MISSING PIECE:
    @FXML
    public void exitApp() {
        System.out.println("Exiting...");
        Platform.exit();
        System.exit(0);
    }
}