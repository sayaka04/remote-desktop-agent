package remoteagent.handler;

import remoteagent.Main;

import javax.imageio.ImageIO;

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

import java.util.logging.Level;
import java.util.logging.Logger;

public final class ActionHandler {

    private static final Logger LOGGER =
            Logger.getLogger(Main.class.getName());

    private ActionHandler() {
        throw new UnsupportedOperationException("Utility class");
    }





    // -----------------------------------------------
    // Mouse Actions
    // -----------------------------------------------

    // --- Mouse click
    public static void handleClick(Robot robot, String button) {
        int mask = switch (button != null ? button : "left") {
            case "right" -> InputEvent.BUTTON3_DOWN_MASK;
            case "middle" -> InputEvent.BUTTON2_DOWN_MASK;
            default -> InputEvent.BUTTON1_DOWN_MASK;
        };
        robot.mousePress(mask);
        robot.mouseRelease(mask);
    }

    // --- Mouse reposition
    public static void handleMove(Robot robot, int x, int y) {
        robot.mouseMove(x, y);
    }







    // -----------------------------------------------
    // Keyboard Actions
    // -----------------------------------------------

    // --- Keyboard type texts
    public static void handleType(Robot robot, String text) {
        if (text == null) return;
        for (char c : text.toCharArray()) {
            ActionHandler.typeCharacter(robot, c); // Using a helper for keyboard logic
        }
    }

    // --- Keyboard type one character
    private static void typeCharacter(Robot robot, char c){
        int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
        if (c == ' ') keyCode = KeyEvent.VK_SPACE;
        if (c == '!') {
            robot.keyPress(KeyEvent.VK_SHIFT);
            robot.keyPress(KeyEvent.VK_1);
            robot.keyRelease(KeyEvent.VK_1);
            robot.keyRelease(KeyEvent.VK_SHIFT);
            return;
        }
        // --- Special cases (e.g., ?, @, :)
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

    public static void handleCtrlA(Robot robot) {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    public static void handleCtrlC(Robot robot) {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_C);
        robot.keyRelease(KeyEvent.VK_C);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    public static void handleCtrlV(Robot robot) {
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    // --- Paste from text received
    public static void handlePaste(Robot robot, String text) {
        if (text == null) return;

        // 1. Copy to clipboardhandleSelectAll
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);

        // 2. Paste using Robot (Ctrl + V)
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }







    // -----------------------------------------------
    // Special Action
    // -----------------------------------------------

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

}
