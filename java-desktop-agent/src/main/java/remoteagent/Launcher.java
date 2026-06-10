package remoteagent;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class Launcher {

    private static FileLock lock;
    private static FileChannel channel;
    private static RandomAccessFile randomAccessFile;

    public static void main(String[] args) {
        // Check if an instance is already running before launching the application
        if (!checkAndLockInstance()) {
            System.out.println("Another instance of Remote Agent is already running. Exiting...");
            System.exit(0);
            return;
        }

        // This bypasses the strict JavaFX runtime check
        Main.main(args);
    }

    private static boolean checkAndLockInstance() {
        try {
            // Creates a hidden lock file in the user's home directory
            File lockFile = new File(System.getProperty("user.home"), ".remoteagent_app.lock");

            randomAccessFile = new RandomAccessFile(lockFile, "rw");
            channel = randomAccessFile.getChannel();

            // Attempt to acquire an exclusive lock on the file
            lock = channel.tryLock();

            if (lock == null) {
                // If tryLock() returns null, another instance of the app already holds the lock
                return false;
            }

            // Register a shutdown hook to release the lock and clean up the file when the app closes normally
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (lock != null) lock.release();
                    if (channel != null) channel.close();
                    if (randomAccessFile != null) randomAccessFile.close();
                    lockFile.delete();
                } catch (Exception ignored) {
                }
            }));

            return true;
        } catch (Exception e) {
            System.err.println("Failed to acquire application lock: " + e.getMessage());
            return false;
        }
    }
}