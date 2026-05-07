package remoteagent;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER =
            Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {



        getScreenSize();



//        String json = """
//            {
//              "client_payload": {
//                "actions": [
//                  {
//                    "type": "move_mouse",
//                    "x": 500,
//                    "y": 300
//                  },
//                  {
//                    "type": "click",
//                    "button": "left"
//                  },
//                  {
//                    "type": "type_text",
//                    "text": "Hello Desktop!"
//                  }
//                ]
//              }
//            }
//        """;
//
//        String response = null;
//        try {
//            response = ApiClient.postJson("/commands/1/request", json);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//        System.out.println(response);

//        ApiController apiController = new ApiController();
//        apiController.respond();
//
//        JsonObject json = Json.parseObject(apiController.getNewData());
//        JsonObject data = Json.getObject(json, "data");
//        System.out.println(Json.toJson(json));
//        if(data != null && Json.getBool(data, "has_host_response")){
//System.out.println("Oo naa");
//}else{
//    System.out.println("Oo wala");
//
//}
//        if (takeScreenshot()) {
//            System.out.println("Screenshot indeed taken!");
//            printConfig();
//        }

        try {
            final Robot robot = new Robot();

        robot.setAutoDelay(50); // Small delay so the OS can keep up
        ApiController apiController = new ApiController();
            int currentIntervalSec = 1;
            final int MAX_INTERVAL_SEC = 30;
            long lastActiveTime = System.currentTimeMillis();

            while (true) {
                String fetchedData = apiController.getNewData();
                JsonObject json = Json.parseObject(fetchedData);
                JsonObject data = Json.getObject(json, "data");
                assert data != null;
                System.out.println(fetchedData);

                if (Json.getBool(data, "has_client_request")) {
                    System.out.println(Json.toJson(json));
                    System.out.println("Oo naa");

                    JsonObject payload =  Json.getObject(data,"payload");
                    System.out.println(Json.toJson(payload));

                    JsonArray actions = Json.getArray(payload, "actions");
                    System.out.println(Json.toJson(actions));

                    if (actions != null) {
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
                                            typeChar(robot, c); // Using a helper for keyboard logic
                                        }
                                    }
                                    break;

                                //TODO: Add new type called paste_text and perhaps Ctrl+A too and Ctrl+C and Ctrl-V
//                            case "type_text":
//                                String textToType = Json.getString(actionObj, "text");
//                                System.out.println("Pasting text: " + textToType);
//                                pasteText(robot, textToType);
//                                break;
                            }
                        });
                    }

                    if (takeScreenshot()) {
                        System.out.println("Screenshot indeed taken!");
                        apiController.respond();
                        printConfig();
                    }

                    // RESET INTERVAL: fast
                    currentIntervalSec = 1;
                    lastActiveTime = System.currentTimeMillis();

                } else {
                    System.out.println("Oo wala");

                    // INACTIVITY BACKOFF LOGIC
                    long inactiveDurationMs = System.currentTimeMillis() - lastActiveTime;
                    long twoMinutesMs = 2 * 60 * 1000; // 120,000 milliseconds

                    if (inactiveDurationMs >= twoMinutesMs) {
                        // 2 minutes have passed! Start increasing by 2 seconds
                        currentIntervalSec += 2;

                        // Cap the maximum interval
                        if (currentIntervalSec > MAX_INTERVAL_SEC) {
                            currentIntervalSec = MAX_INTERVAL_SEC;
                        }
                    } else {
                        // Still inside the 2-minute window.
                        currentIntervalSec = 1;
                    }
                }

                // SLEEP LOGIC: Pause the loop for the calculated amount of time
                try {
                    System.out.println("Sleeping for " + currentIntervalSec + " seconds...");
                    Thread.sleep(currentIntervalSec * 1000L);
                } catch (InterruptedException e) {
                    System.out.println("Polling interrupted!");
                    Thread.currentThread().interrupt();
                    break; // Safely break the loop if the application is closing
                }

                // return;
            }

        } catch (AWTException e) {
            throw new RuntimeException(e);
        }

    }


    private static void pasteText(Robot robot, String text) {
        if (text == null) return;

        // 1. Copy to clipboard
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);

        // 2. Paste using Robot (Ctrl + V)
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    private static void typeChar(Robot robot, char c) {
        int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);

        // Manual overrides for characters that often fail
        if (c == ' ') keyCode = KeyEvent.VK_SPACE;
        if (c == '!') {
            robot.keyPress(KeyEvent.VK_SHIFT);
            robot.keyPress(KeyEvent.VK_1);
            robot.keyRelease(KeyEvent.VK_1);
            robot.keyRelease(KeyEvent.VK_SHIFT);
            return;
        }
        // Add more special cases here if needed (e.g., ?, @, :)

        if (keyCode != KeyEvent.VK_UNDEFINED) {
            try {
                boolean isUpper = Character.isUpperCase(c);
                if (isUpper) robot.keyPress(KeyEvent.VK_SHIFT);

                robot.keyPress(keyCode);
                robot.keyRelease(keyCode);

                if (isUpper) robot.keyRelease(KeyEvent.VK_SHIFT);
            } catch (IllegalArgumentException e) {
                System.err.println("Could not type char: " + c + " (Code: " + keyCode + ")");
            }
        }
    }



    public static void printConfig(){
        String apiBaseUrl = Config.get("api.base_url");
        String apiToken = Config.get("api.token");
        String deviceUUID = Config.get("api.device_uuid");

        int agentPollInterval = Config.getInt("agent.poll_interval");
        int agentTimeout = Config.getInt("agent.timeout");

        System.out.println("\n--- CONFIG PROPERTIES ---"
                + "\napiBaseUrl: " + apiBaseUrl
                + ",\napiToken: " + apiToken
                + ",\ndeviceUUID: " + deviceUUID
                + ",\nagentPollInterval: " + agentPollInterval
                + ",\nagentTimeout: " + agentTimeout
        );
    }


    public static boolean takeScreenshot() {
        try {
            Rectangle screenRect = new Rectangle(
                    Toolkit.getDefaultToolkit().getScreenSize()
            );

            BufferedImage capture = new Robot()
                    .createScreenCapture(screenRect);

            File imageFile = new File("single-screen.png");
            ImageIO.write(capture, "png", imageFile);

            if (imageFile.exists()) {
                LOGGER.info("Screenshot saved: " + imageFile.getAbsolutePath());
                return true;
            } else {
                LOGGER.warning("Failed to save screenshot.");
            }

        } catch (AWTException | IOException e) {
            LOGGER.log(Level.SEVERE, "Error taking screenshot", e);
        }

        return false;
    }



    public static void getScreenSize(){
        // Get the default toolkit
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

// Access the width and height
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        System.out.println("Screen Resolution: " + width + "x" + height);
    }

}