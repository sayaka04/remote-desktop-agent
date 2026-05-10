package remoteagent.ui;

import javafx.application.Platform;
import remoteagent.handler.JobHandler;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class TrayManager {

    private final Runnable onOpen;
    private final Runnable onHide;
    private final JobHandler handler;

    public TrayManager(Runnable onOpen, Runnable onHide, JobHandler handler) {
        this.onOpen = onOpen;
        this.onHide = onHide;
        this.handler = handler;
    }

    public void init() {
        if (!SystemTray.isSupported()) return;

        try {
            SystemTray tray = SystemTray.getSystemTray();

            Image image = Toolkit.getDefaultToolkit().createImage("pc1.jpg");

            if (image == null) {
                image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
            }

            TrayIcon trayIcon = getTrayIcon(image);

            trayIcon.addActionListener(e ->
                    Platform.runLater(onOpen)
            );

            tray.add(trayIcon);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TrayIcon getTrayIcon(Image image) {
        PopupMenu menu = new PopupMenu();

        MenuItem openItem = new MenuItem("Open");
        MenuItem hideItem = new MenuItem("Hide");
        MenuItem exitItem = new MenuItem("Exit");

        openItem.addActionListener(e ->
                Platform.runLater(onOpen)
        );

        hideItem.addActionListener(e ->
                Platform.runLater(onHide)
        );

        exitItem.addActionListener(e -> {
            handler.stop();
            Platform.exit();
            System.exit(0);
        });

        menu.add(openItem);
        menu.add(hideItem);
        menu.addSeparator();
        menu.add(exitItem);

        TrayIcon trayIcon = new TrayIcon(image, "Remote Agent", menu);
        trayIcon.setImageAutoSize(true);
        return trayIcon;
    }
}