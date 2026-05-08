    package remoteagent.handler;

    import com.google.gson.JsonArray;
    import com.google.gson.JsonObject;
    import remoteagent.api.ApiController;
    import remoteagent.utils.Action;
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

                    case "move_mouse":
                        int x = Json.getInt(actionObj, "x");
                        int y = Json.getInt(actionObj, "y");
                        robot.mouseMove(x, y);
                        break;

                    case "click":
                        String button = Json.getString(actionObj, "button");
                        int mask = button.equals("right") ? InputEvent.BUTTON3_DOWN_MASK : InputEvent.BUTTON1_DOWN_MASK;
                        robot.mousePress(mask);
                        robot.mouseRelease(mask);
                        break;
                    case "type_text":
                        String text = Json.getString(actionObj, "text");
                        if (text != null) {
                            for (char c : text.toCharArray()) {
                                Action.typeChar(robot, c); // Using a helper for keyboard logic
                            }
                        }
                        break;
                    //TODO: Add new type called paste_text and perhaps Ctrl+A too and Ctrl+C and Ctrl-V
                    //  case "type_text":
                    //      String textToType = Json.getString(actionObj, "text");
                    //      System.out.println("Pasting text: " + textToType);
                    //      pasteText(robot, textToType);
                    //      break;
                }
            });
        }

        private void performJob(Robot robot){
            // --- 1. Request data
            JsonObject data = apiController.requestData();

            // --- 2. Check for Actionable commands
            if (Json.getBool(data, "has_client_request")) {

                // --- PATH A: Active Task Execution ---

                // --- 3A. Get array of actions
                JsonArray actions = getActions(data);

                // --- 4A. Perform each actions accordingly
                if (actions != null) {
                    performActions(actions, robot);
                }

                // --- 5A. Take screenshot
                if (Action.takeScreenshot()) {

                    //TODO: Remove print
                    System.out.println("Screenshot indeed taken!");

                    // --- 6A. Return screenshot and success message
                    apiController.respond();
                }

                // --- 7A. Reset interval, system will requestData straight for 2 minutes
                pollingTimer.reset();
            } else {
                // --- PATH B: Inactivity Backoff Logic ---
                pollingTimer.update();
            }

            // --- Finally. REJOIN: Pause the loop for the calculated amount of time (Sleep Logic)
            pollingTimer.sleep();
        }

    }