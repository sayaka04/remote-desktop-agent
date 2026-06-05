package remoteagent.handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import remoteagent.api.ApiController;
import remoteagent.controller.WindowController;
import remoteagent.utils.Json;
import remoteagent.utils.LogUtil;

import java.awt.*;

public class JobHandler {

    boolean isRunning = false;
    String currentState = "Idle"; // Tracks whether the worker is Active or Idle

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
//            System.out.println("[JobHandler] Start request blocked: Worker thread is already alive.");
            return;
        }
        isRunning = true;
        currentState = "Idle"; // Reset to Idle when starting
        workerThread = new Thread(() -> {
            try {
//                System.out.println("[JobHandler] Initializing Robot...");
                Robot robot = new Robot();
                robot.setAutoDelay(50);
                while (isRunning) {
                    // Append Poll Rate and State to the log output
                    windowController.writeToLog(LogUtil.LogType.INFO, "Running: " + isRunning + " | Poll Rate: " + pollingTimer.currentIntervalSec + "s | State: " + currentState);
//                    System.out.println("\n[JobHandler] --- New Loop Cycle (State: " + currentState + ", Poll Rate: " + pollingTimer.currentIntervalSec + "s) ---");

                    performJob(robot);
                }
                windowController.writeToLog(LogUtil.LogType.INFO, "Worker loop exited.");
//                System.out.println("[JobHandler] Worker loop cleanly exited.");

            } catch (AWTException e) {
//                System.out.println("[JobHandler] CRITICAL FATAL: Robot instantiation failed.");
                throw new RuntimeException(e);
            }
        });
        workerThread.start();
        windowController.writeToLog(LogUtil.LogType.WARN, "Worker started.");
//        System.out.println("[JobHandler] Worker started successfully.");
    }

    public void stop() {
        isRunning = false;
        if (workerThread != null) {
            workerThread.interrupt();
        }
        windowController.writeToLog(LogUtil.LogType.WARN, "Stopped.");
//        System.out.println("[JobHandler] Worker stop requested.");
    }



    private JsonArray getActions(JsonObject data) {
        // --- 1 & 2. Get payload and return its actions array
        JsonObject payload = Json.getObject(data, "payload");
        return payload != null ? Json.getArray(payload, "actions") : null;
    }

    private void performActions(JsonArray actions, Robot robot) {
        if (actions == null || actions.isEmpty()) return;

        windowController.writeToLog(LogUtil.LogType.INFO, "Executing " + actions.size() + " actions...");
//        System.out.println("[JobHandler] Executing " + actions.size() + " actions...");

        for (JsonElement element : actions) {
            if (element.isJsonObject()) {
                // Call our new dispatcher for every action in the array
                ActionHandler.execute(robot, element.getAsJsonObject());

                // Small delay between actions to simulate human speed
                // and prevent the OS from dropping events
                robot.delay(10);
            }
        }
//        System.out.println("[JobHandler] Action execution complete.");
    }

    private void performJob(Robot robot){
        try {
            System.out.println("[JobHandler] Requesting data from server...");
            // --- 1. Request data
            JsonObject data = apiController.requestData();

            // --- 2. Check for Actionable commands
            if (data != null && Json.getBool(data, "has_client_request")) {
//                System.out.println("[JobHandler] Active request detected! State -> Active.");
                currentState = "Active";

                // --- PATH A: Active Task Execution ---

                // --- 3A. Get array of actions
                JsonArray actions = getActions(data);

                // --- 4A. Perform each actions accordingly
                if (actions != null) {
                    performActions(actions, robot);
                }

                // --- 5A. Take screenshot
                System.out.println("[JobHandler] Attempting to take screenshot...");
                if (ActionHandler.takeScreenshot()) {

                    //TODO: Remove print
                    windowController.writeToLog(LogUtil.LogType.WARN, "Screenshot taken.");
//                    System.out.println("[JobHandler] Screenshot successful, responding to server...");

                    // --- 6A. Return screenshot and success message
                    apiController.respond();
                } else {
                    windowController.writeToLog(LogUtil.LogType.WARN, "Screenshot failed.");
//                    System.out.println("[JobHandler] Screenshot failed.");
                }

                // --- 7A. Reset interval, system will requestData straight for 2 minutes
//                System.out.println("[JobHandler] Resetting polling timer due to activity.");
                pollingTimer.reset();
            } else {
//                System.out.println("[JobHandler] No commands from server. State -> Idle.");
                currentState = "Idle";
                // --- PATH B: Inactivity Backoff Logic ---
                // --- Also Can be triggered by sudden lost of connection, or api exceptions
                pollingTimer.update();
            }
        } catch (Exception e) {
            // try-catch prevents the app from crashing when the server is offline!
//            System.out.println("[JobHandler] Connection Error (Server Offline?): " + e.getMessage());
//            System.out.println("[JobHandler] Safely routing into Idle Backoff logic.");
            currentState = "Idle";
            pollingTimer.update();
        } finally {
            // --- Finally. REJOIN: Pause the loop for the calculated amount of time (Sleep Logic)
//            System.out.println("[JobHandler] Entering sleep phase for " + pollingTimer.currentIntervalSec + "s...");
            pollingTimer.sleep();
        }
    }


}