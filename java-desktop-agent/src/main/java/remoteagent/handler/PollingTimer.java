package remoteagent.handler;

public class PollingTimer {

    int currentIntervalSec = 1;
    final int MAX_INTERVAL_SEC = 30;
    long lastActiveTime = System.currentTimeMillis();

    public void reset(){
        currentIntervalSec = 1;
        lastActiveTime = System.currentTimeMillis();
    }

    public void update() {
        // --- Calculate time since last activity
        long inactiveDurationMs = System.currentTimeMillis() - lastActiveTime;
        long twoMinutesMs = 2 * 60 * 1000; // 120,000 milliseconds

        // --- Evaluate if 2 minutes have passed
        if (inactiveDurationMs >= twoMinutesMs) {
            // --- Start increasing interval by 2 seconds, capped at MAX
            currentIntervalSec += 2;
            if (currentIntervalSec > MAX_INTERVAL_SEC) {
                currentIntervalSec = MAX_INTERVAL_SEC;
            }
        } else {
            // --- Still inside the 2-minute window, keep interval at 1s
            currentIntervalSec = 1;
        }
    }

    public void sleep() {
        try {
            //TODO: Add debug mode
            System.out.println("Sleeping for " + currentIntervalSec + " seconds...");
            Thread.sleep(currentIntervalSec * 1000L);
        } catch (InterruptedException e) {
            //TODO: Add debug mode
            System.out.println("Polling interrupted!");
            Thread.currentThread().interrupt();
        }
    }

}
