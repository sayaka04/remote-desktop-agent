package remoteagent.handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import remoteagent.Main;
import remoteagent.utils.Json;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ActionHandler {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private ActionHandler() {}

    /**
     * Main Dispatcher: Maps JSON actions to Robot commands
     */
    public static void execute(Robot robot, JsonObject action) {
        String type = Json.getString(action, "type");
        if (type == null) return;

        try {
            switch (type) {
                case "move_mouse" -> handleMove(robot, action);
                case "click" -> handleClick(robot, action, false);
                case "double_click" -> handleClick(robot, action, true);
                case "scroll" -> handleScroll(robot, action);
                case "type_text" -> handleType(robot, Json.getString(action, "text"));
                case "key_press" -> handleKeyPress(robot, action);
                case "key_down" -> handleKeyState(robot, action, true);
                case "key_up" -> handleKeyState(robot, action, false);
                case "hotkey" -> handleHotkey(robot, action);
                default -> LOGGER.warning("Unknown action type: " + type);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Execution error on " + type, e);
        }
    }

    private static void handleMove(Robot robot, JsonObject action) {
        try {
            // 1. Get the percentage values sent from React (e.g., 0.50)
            // Using getAsDouble() directly from Gson's JsonObject
            double xPercent = action.get("x").getAsDouble();
            double yPercent = action.get("y").getAsDouble();

            // 2. Get the actual screen dimensions of the Host machine
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int screenWidth = (int) screenSize.getWidth();
            int screenHeight = (int) screenSize.getHeight();

            // 3. Calculate the exact pixel coordinates
            int targetX = (int) (screenWidth * xPercent);
            int targetY = (int) (screenHeight * yPercent);

            // 4. Move the mouse
            robot.mouseMove(targetX, targetY);

            // Optional: Log it so you can verify the math in your UI
            LOGGER.info(String.format("Mouse moved to X:%d Y:%d (%.2f%%, %.2f%%)", targetX, targetY, xPercent * 100, yPercent * 100));

        } catch (Exception e) {
            LOGGER.warning("Failed to parse mouse coordinates: " + e.getMessage());
        }
    }

    private static void handleClick(Robot robot, JsonObject action, boolean isDouble) {
        String button = Json.getString(action, "button");
        int mask = switch (button != null ? button : "left") {
            case "right" -> InputEvent.BUTTON3_DOWN_MASK;
            case "middle" -> InputEvent.BUTTON2_DOWN_MASK;
            default -> InputEvent.BUTTON1_DOWN_MASK;
        };

        robot.mousePress(mask);
        robot.mouseRelease(mask);
        if (isDouble) {
            robot.delay(50);
            robot.mousePress(mask);
            robot.mouseRelease(mask);
        }
    }

    private static void handleScroll(Robot robot, JsonObject action) {
        // 'amount' usually comes as positive for down, negative for up
        int amount = Json.getInt(action, "amount");
        robot.mouseWheel(amount);
    }

    private static void handleType(Robot robot, String text) {
        if (text == null || text.isEmpty()) return;

        // Using Clipboard for 'type_text' is 100% more reliable than individual key presses
        // as it handles symbols (@, #, !, etc.) regardless of keyboard layout.
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);

        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    private static void handleKeyPress(Robot robot, JsonObject action) {
        int keyCode = getVirtualKey(Json.getString(action, "key"));
        if (keyCode == -1) return;
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
    }

    private static void handleKeyState(Robot robot, JsonObject action, boolean isDown) {
        int keyCode = getVirtualKey(Json.getString(action, "key"));
        if (keyCode == -1) return;
        if (isDown) robot.keyPress(keyCode);
        else robot.keyRelease(keyCode);
    }

    private static void handleHotkey(Robot robot, JsonObject action) {
        String key = Json.getString(action, "key");
        JsonArray modifiers = action.getAsJsonArray("modifiers");
        List<Integer> pressedMods = new ArrayList<>();

        // 1. Press Modifiers
        if (modifiers != null) {
            for (JsonElement mod : modifiers) {
                int modCode = getVirtualKey(mod.getAsString());
                if (modCode != -1) {
                    robot.keyPress(modCode);
                    pressedMods.add(modCode);
                }
            }
        }

        // 2. Press main key
        int mainKey = getVirtualKey(key);
        if (mainKey != -1) {
            robot.keyPress(mainKey);
            robot.keyRelease(mainKey);
        }

        // 3. Release Modifiers in reverse
        for (int i = pressedMods.size() - 1; i >= 0; i--) {
            robot.keyRelease(pressedMods.get(i));
        }
    }

    /**
     * Helper to map String keys (from React/PHP) to Java VK constants
     */
    private static int getVirtualKey(String key) {
        if (key == null) return -1;
        return switch (key.toLowerCase()) {
            case "enter" -> KeyEvent.VK_ENTER;
            case "backspace" -> KeyEvent.VK_BACK_SPACE;
            case "tab" -> KeyEvent.VK_TAB;
            case "shift" -> KeyEvent.VK_SHIFT;
            case "ctrl", "control" -> KeyEvent.VK_CONTROL;
            case "alt" -> KeyEvent.VK_ALT;
            case "escape" -> KeyEvent.VK_ESCAPE;
            case "space" -> KeyEvent.VK_SPACE;
            case "up" -> KeyEvent.VK_UP;
            case "down" -> KeyEvent.VK_DOWN;
            case "left" -> KeyEvent.VK_LEFT;
            case "right" -> KeyEvent.VK_RIGHT;
            case "f1" -> KeyEvent.VK_F1;
            case "f2" -> KeyEvent.VK_F2;
            case "f3" -> KeyEvent.VK_F3;
            case "f4" -> KeyEvent.VK_F4;
            case "f5" -> KeyEvent.VK_F5;
            case "f6" -> KeyEvent.VK_F6;
            case "f7" -> KeyEvent.VK_F7;
            case "f8" -> KeyEvent.VK_F8;
            case "f9" -> KeyEvent.VK_F9;
            case "f10" -> KeyEvent.VK_F10;
            case "f11" -> KeyEvent.VK_F11;
            case "f12" -> KeyEvent.VK_F12;
            // Handle single characters (a-z, 0-9)
            default -> (key.length() == 1) ? KeyEvent.getExtendedKeyCodeForChar(key.charAt(0)) : -1;
        };
    }

    public static boolean takeScreenshot() {
        try {
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage capture = new Robot().createScreenCapture(screenRect);
            File imageFile = new File("single-screen.png");
            return ImageIO.write(capture, "png", imageFile);
        } catch (AWTException | IOException e) {
            LOGGER.log(Level.SEVERE, "Screenshot failure", e);
            return false;
        }
    }
}