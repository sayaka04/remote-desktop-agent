package remoteagent.utils;

import remoteagent.Main;

import javax.imageio.ImageIO;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
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

public final class Action {

    private static final Logger LOGGER =
            Logger.getLogger(Main.class.getName());

    private Action() {
        throw new UnsupportedOperationException("Utility class");
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

    public static void typeChar(Robot robot, char c) {
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
