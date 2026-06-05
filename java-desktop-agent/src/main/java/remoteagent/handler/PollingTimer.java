package remoteagent.handler;

import remoteagent.utils.Config;

public class PollingTimer {

    int currentIntervalSec = 1;
    int maxIntervalSec = Config.getInt("poll.max_interval_sec");
    long timeoutSec = Config.getInt("poll.timeout_sec")*1000; // 120,000 milliseconds

    long lastActiveTime = System.currentTimeMillis();

    public void reset(){
        currentIntervalSec = 1;
        lastActiveTime = System.currentTimeMillis();
//        System.out.println("[PollingTimer] Timer reset. Interval back to 1s.");
    }

    public void update() {
        // --- Calculate time since last activity
        long inactiveDurationMs = System.currentTimeMillis() - lastActiveTime;
//        System.out.println("[PollingTimer] Inactive Duration: " + (inactiveDurationMs/1000) + "s / Timeout Threshold: " + (timeoutSec/1000) + "s");

        // --- Evaluate if 2 minutes have passed
        if (inactiveDurationMs >= timeoutSec) {
            // --- Start increasing interval by 2 seconds, capped at MAX
            currentIntervalSec += 2;
            if (currentIntervalSec > maxIntervalSec) {
                currentIntervalSec = maxIntervalSec;
            }
//            System.out.println("[PollingTimer] Timeout passed! Interval increasing. Now: " + currentIntervalSec + "s (Capped at: " + maxIntervalSec + "s)");
        } else {
            // --- Still inside the 2-minute window, keep interval at 1s
            currentIntervalSec = 1;
//            System.out.println("[PollingTimer] Still inside the initial active window. Interval holding at 1s.");
        }
    }

    public void sleep() {
        try {
            //TODO: Add debug mode
//            System.out.println("Sleeping for " + currentIntervalSec + " seconds...");
            Thread.sleep(currentIntervalSec * 1000L);
        } catch (InterruptedException e) {
            //TODO: Add debug mode
//            System.out.println("Polling interrupted!");
            Thread.currentThread().interrupt();
        }
    }

    public void reloadConfig(){
        // FIXED: Removed * 1000 here as well to match logic
        maxIntervalSec = Config.getInt("poll.max_interval_sec");
        timeoutSec = Config.getInt("poll.timeout_sec") * 1000;

//        System.out.println("Reloaded:");
//        System.out.println("maxIntervalSec=" + maxIntervalSec);
//        System.out.println("timeoutSec=" + timeoutSec);
    }

}