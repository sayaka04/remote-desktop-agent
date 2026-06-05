package remoteagent.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import remoteagent.controller.WindowController;
import remoteagent.handler.JobHandler;
import remoteagent.utils.LogUtil;
import java.net.URL;

public class WindowManager {

    private Stage stage;
    private WindowController controller;
    private final JobHandler handler;

    public WindowManager(JobHandler handler) {
        this.handler = handler;
    }

    public void init(Stage stage) {
        this.stage = stage;

        try {
            URL url = getClass().getResource("/remoteagent/fxml/Window.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            controller = loader.getController();
            controller.setJobHandler(handler);
            handler.setWindowController(controller);

            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Remote Agent");

            // MODIFIED: Load image from resources instead of local file system
            URL iconUrl = getClass().getResource("/pc1.png");
            if (iconUrl != null) {
                stage.getIcons().add(new Image(iconUrl.toExternalForm()));
            } else {
                System.err.println("Window icon image not found in resources.");
            }

            stage.setOnCloseRequest(e -> {
                e.consume();
                hideWindow();
                controller.writeToLog(LogUtil.LogType.INFO,"Window hidden to system tray.");
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showWindow() {
        if (stage != null) {
            stage.show();
            stage.setIconified(false);
            stage.toFront();

            if (controller != null)
                controller.writeToLog(LogUtil.LogType.INFO, "Window restored");
        }
    }

    public void hideWindow() {
        if (stage != null) {
            stage.hide();
        }
    }
}