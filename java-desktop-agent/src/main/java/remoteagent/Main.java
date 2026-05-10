package remoteagent;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import remoteagent.handler.JobHandler;
import remoteagent.ui.TrayManager;
import remoteagent.ui.WindowManager;

public class Main extends Application {

    private final JobHandler handler = new JobHandler();

    private WindowManager windowManager;
    private TrayManager trayManager;

    @Override
    public void start(Stage primaryStage) {

        Platform.setImplicitExit(false);

        windowManager = new WindowManager(handler);
        windowManager.init(primaryStage);

        trayManager = new TrayManager(
                windowManager::showWindow,
                windowManager::hideWindow,
                handler
        );

        trayManager.init();

        windowManager.showWindow();
    }

    public static void main(String[] args) {
        launch(args);
    }
}