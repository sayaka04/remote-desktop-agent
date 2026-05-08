    package remoteagent.handler;

    import com.google.gson.JsonArray;
    import com.google.gson.JsonObject;
    import remoteagent.api.ApiController;
    import remoteagent.utils.Json;

    import java.awt.*;
    import java.awt.event.InputEvent;
    import java.util.Objects;

    public class JobHandler {

        boolean isRunning = false;

        ApiController apiController = new ApiController();
        PollingTimer pollingTimer = new PollingTimer();

        public void start(){
            if (isRunning) return;
            isRunning = true;

            try {
                final Robot robot = new Robot();
                robot.setAutoDelay(50);                 // --- Small delay so the OS can keep up

                while (isRunning) {
                    performJob(robot);
                }

            } catch (AWTException e) {
                throw new RuntimeException(e);
            }
        }

        public void stop(){
            isRunning = false;
        }



        private JsonArray getActions(JsonObject data) {
            // --- 1 & 2. Get payload and return its actions array
            JsonObject payload = Json.getObject(data, "payload");
            return payload != null ? Json.getArray(payload, "actions") : null;
        }

        public void performActions(JsonArray actions, Robot robot){
            actions.forEach(action -> {
                JsonObject actionObj = action.getAsJsonObject();
                System.out.println("Action Type: " + Json.getString(actionObj, "type"));
                String type = Json.getString(actionObj, "type");
                switch (Objects.requireNonNull(type)) {
                    case "move_mouse" ->
                            ActionHandler.handleMove(robot, Json.getInt(actionObj, "x"), Json.getInt(actionObj, "y"));
                    case "click" ->
                            ActionHandler.handleClick(robot, Json.getString(actionObj, "button"));
                    case "type_text" ->
                            ActionHandler.handleType(robot, Json.getString(actionObj, "text"));
                    case "paste_text" ->
                            ActionHandler.handlePaste(robot, Json.getString(actionObj, "text"));
                    case "select_all" ->
                            ActionHandler.handleCtrlA(robot);
                }
            });
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
                    System.out.println("Screenshot indeed taken!");

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