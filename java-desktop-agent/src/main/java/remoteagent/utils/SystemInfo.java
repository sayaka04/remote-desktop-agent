package remoteagent.utils;

import java.awt.*;

public class SystemInfo {

    public static void printConfig(){
        String apiBaseUrl = Config.get("api.base_url");
        String apiToken = Config.get("api.token");
        String deviceID = Config.get("api.device_id");

        int agentPollInterval = Config.getInt("agent.poll_interval");
        int agentTimeout = Config.getInt("agent.timeout");

        System.out.println("\n--- CONFIG PROPERTIES ---"
                + "\napiBaseUrl: " + apiBaseUrl
                + ",\napiToken: " + apiToken
                + ",\ndeviceID: " + deviceID
                + ",\nagentPollInterval: " + agentPollInterval
                + ",\nagentTimeout: " + agentTimeout
        );
    }

    public static void getScreenSize(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        System.out.println("Screen Resolution: " + width + "x" + height);
    }
}
