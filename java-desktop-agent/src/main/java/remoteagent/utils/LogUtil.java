package remoteagent.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogUtil {

    // Define the types of logs you want to support
    public enum LogType {
        INFO,
        WARN,
        ERROR,
        SUCCESS
    }

    // Define how the date and time should look: [YYYY-MM-DD HH:MM:SS]
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Formats a log message with the current date, time, and log type.
     * * @param type    The type of log (e.g., LogType.INFO)
     * @param message The message to display
     * @return A formatted string ready to be appended to your TextArea
     */
    public static String format(LogType type, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);

        // Example output: [2026-05-10 16:01:12] [INFO] System ready...
        return String.format("[%s] [%s] | %s", timestamp, type.name(), message + "\n");

        // Note: %-7s ensures the brackets align nicely even if the word "INFO"
        // is shorter than "SUCCESS"
    }
}