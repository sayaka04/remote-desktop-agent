    package remoteagent.handler;

    import com.google.gson.JsonArray;
    import com.google.gson.JsonElement;
    import com.google.gson.JsonObject;
    import remoteagent.api.ApiController;
    import remoteagent.controller.WindowController;
    import remoteagent.utils.Json;
    import remoteagent.utils.LogUtil;

    import java.awt.*;
    import java.awt.event.InputEvent;
    import java.util.Objects;

    public class JobHandler {

        boolean isRunning = false;

        ApiController apiController = new ApiController();
        public PollingTimer pollingTimer = new PollingTimer();
        private Thread workerThread;

        WindowController windowController;
        public void setWindowController(WindowController windowController){
            this.windowController = windowController;
            apiController.setWindowController(this.windowController);
        }

        public void start() {
            // Prevent duplicate threads
            if (workerThread != null && workerThread.isAlive()) {
                windowController.writeToLog(LogUtil.LogType.WARN, "Already running.");
                return;
            }
            isRunning = true;
            workerThread = new Thread(() -> {
                try {
                    Robot robot = new Robot();
                    robot.setAutoDelay(50);
                    while (isRunning) {
                        windowController.writeToLog(LogUtil.LogType.INFO, "Running: " + isRunning);
                        performJob(robot);
                    }
                    windowController.writeToLog(LogUtil.LogType.INFO, "Worker loop exited.");

                } catch (AWTException e) {
                    throw new RuntimeException(e);
                }
            });
            workerThread.start();
            windowController.writeToLog(LogUtil.LogType.WARN, "Worker started.");
        }

        public void stop() {
            isRunning = false;
            if (workerThread != null) {
                workerThread.interrupt();
            }
            windowController.writeToLog(LogUtil.LogType.WARN, "Stopped.");
        }



        private JsonArray getActions(JsonObject data) {
            // --- 1 & 2. Get payload and return its actions array
            JsonObject payload = Json.getObject(data, "payload");
            return payload != null ? Json.getArray(payload, "actions") : null;
        }

        private void performActions(JsonArray actions, Robot robot) {
            if (actions == null || actions.isEmpty()) return;

            windowController.writeToLog(LogUtil.LogType.INFO, "Executing " + actions.size() + " actions...");

            for (JsonElement element : actions) {
                if (element.isJsonObject()) {
                    // Call our new dispatcher for every action in the array
                    ActionHandler.execute(robot, element.getAsJsonObject());

                    // Small delay between actions to simulate human speed
                    // and prevent the OS from dropping events
                    robot.delay(10);
                }
            }
        }

        private void performJob(Robot robot){
            // --- 1. Request data
            JsonObject data = apiController.requestData();

            // --- 2. Check for Actionable commands
            if (data != null && Json.getBool(data, "has_client_request")) {

                // --- PATH A: Active Task Execution ---

                // --- 3A. Get array of actions
                JsonArray actions = getActions(data);

                // --- 4A. Perform each actions accordingly
                if (actions != null) {
                    performActions(actions, robot);
                }

                // --- 5A. Take screenshot
                if (ActionHandler.takeScreenshot()) {

                    //TODO: Remove print
                    windowController.writeToLog(LogUtil.LogType.WARN, "Screenshot taken.");

                    // --- 6A. Return screenshot and success message
                    apiController.respond();
                }

                // --- 7A. Reset interval, system will requestData straight for 2 minutes
                pollingTimer.reset();
            } else {
                // --- PATH B: Inactivity Backoff Logic ---
                // --- Also Can be triggered by sudden lost of connection, or api exceptions
                pollingTimer.update();
            }

            // --- Finally. REJOIN: Pause the loop for the calculated amount of time (Sleep Logic)
            pollingTimer.sleep();
        }


    }