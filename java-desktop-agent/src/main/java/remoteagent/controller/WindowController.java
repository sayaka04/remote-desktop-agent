package remoteagent.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.application.Platform; // Add this import
import javafx.scene.control.ToggleButton;
import remoteagent.handler.JobHandler;
import remoteagent.utils.LogUtil;

public class WindowController {


    private static final int MAX_LOG_LINES = 20;
    private int currentLineCount = 0;

    @FXML
    private TextArea logArea;

    @FXML
    public void initialize() {
        System.out.println("Controller loaded!");
        writeToLog(LogUtil.LogType.INFO, "System ready and Controller linked!");
    }

    @FXML
    public void handleOpenConfig() {
        System.out.println("Opening config...");
        // Add your config logic here
    }

    public void writeToLog(LogUtil.LogType type, String message) {
        // Platform.runLater ensures this works even if called from a background thread
        Platform.runLater(() -> {
            logArea.appendText(LogUtil.format(type, message));

            int totalRows = logArea.getParagraphs().size();
            System.out.println("Total rows: " + totalRows);
            if (totalRows > MAX_LOG_LINES) {
                // --- 1. Get every single piece of text currently in the box
                String allText = logArea.getText();
                // --- 2. Find exactly where the first line ends
                int firstNewline = allText.indexOf('\n');
                // --- 3. Make sure a newline actually exists
                if (firstNewline != -1) {
                    // --- 4. Grab everything AFTER that first newline and overwrite the text area
                    logArea.setText(allText.substring(firstNewline + 1));
                }
            }
        });
    }

    // THIS IS THE MISSING PIECE:
    @FXML
    public void exitApp() {
        writeToLog(LogUtil.LogType.INFO, "Exiting!");
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private ToggleButton toggleSwitchProcess;

    @FXML
    public void handleToggleProcess() {
        if (toggleSwitchProcess.isSelected()) {
            toggleSwitchProcess.setText("Close Process");
            jobHandler.start();
            writeToLog(LogUtil.LogType.INFO, "Process turned on!");
            toggleSwitchProcess.getStyleClass().remove("btn-off");
            toggleSwitchProcess.getStyleClass().add("btn-on");
        } else {
            toggleSwitchProcess.setText("Start Process");
            jobHandler.stop();
            writeToLog(LogUtil.LogType.INFO, "Process turned off!");
            toggleSwitchProcess.getStyleClass().remove("btn-on");
            toggleSwitchProcess.getStyleClass().add("btn-off");
        }
    }
    JobHandler jobHandler;
    public void setJobHandler(JobHandler jobHandler){
        this.jobHandler = jobHandler;
    }
}