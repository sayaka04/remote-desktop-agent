package remoteagent;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import remoteagent.controller.WindowController;
import remoteagent.handler.JobHandler;

import java.awt.*;
import java.net.URL;

public class Main extends Application {

    private static final JobHandler handler = new JobHandler();
    private Stage stage;
    private WindowController controller;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        // 1. Keep the JavaFX engine running in the background
        Platform.setImplicitExit(false);

        // 2. Load FXML from Resources (The standard way)
        // This looks in src/main/resources/remoteagent/fxml/Window.fxml
        URL url = getClass().getResource("/remoteagent/fxml/Window.fxml");

        if (url == null) {
            // Error handling: If Gradle didn't copy the file, the URL will be null
            System.err.println("FATAL ERROR: Could not find Window.fxml in resources!");
            System.err.println("Check: src/main/resources/remoteagent/fxml/Window.fxml");
            Platform.exit();
            return;
        }

        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();

        // 3. Link the Controller
        this.controller = loader.getController();

        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("Remote Agent");

        // 4. Intercept Close: Hide instead of Exit
        stage.setOnCloseRequest(e -> {
            e.consume();
            stage.hide();
            if (controller != null) controller.writeToLog("Window hidden to system tray.");
        });

        // 5. Setup System Tray & Background Threads
        initTray();
        new Thread(handler::start).start();

        // 6. Final Launch
        stage.show();
        if (controller != null) controller.writeToLog("Remote Agent Started Successfully.");
    }

    private void initTray() {
        if (!SystemTray.isSupported()) return;

        try {
            SystemTray tray = SystemTray.getSystemTray();

            // Note: pc1.jpg should be in your project root or src/main/resources
            Image image = Toolkit.getDefaultToolkit().createImage("pc1.jpg");

            // Fallback if image is missing to prevent tray from being invisible
            if (image == null) {
                image = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_RGB);
            }

            PopupMenu menu = new PopupMenu();
            MenuItem openItem = new MenuItem("Open");
            MenuItem exitItem = new MenuItem("Exit");

            openItem.addActionListener(e -> Platform.runLater(this::restoreWindow));

            exitItem.addActionListener(e -> {
                handler.stop();
                Platform.exit();
                System.exit(0);
            });

            menu.add(openItem);
            menu.addSeparator();
            menu.add(exitItem);

            TrayIcon trayIcon = new TrayIcon(image, "Remote Agent", menu);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> Platform.runLater(this::restoreWindow));

            tray.add(trayIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restoreWindow() {
        if (stage != null) {
            stage.show();
            stage.setIconified(false);
            stage.toFront();
            stage.requestFocus();
            if (controller != null) controller.writeToLog("Window restored from tray.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}