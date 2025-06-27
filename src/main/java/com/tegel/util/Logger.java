package com.tegel.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple utility class for logging in the Tegel application.
 * Provides methods to log information, warnings, errors, and debug messages
 * with timestamps and source identifiers
 */
public class Logger {

    private static final String LOG_DIRECTORY =
            System.getProperty("catalina.base") + "/logs/tegel/";
    private static final String LOG_FILE = LOG_DIRECTORY + "tegel-app.log";
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    static {
        // Create the log directory if it doesn't exist
        try {
            File logDir = new File(LOG_DIRECTORY);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
        } catch (RuntimeException e) {
            System.err.println("Failed to create log directory: " + e.getMessage());
        }
    }

    /**
     * Log an informational message.
     *
     * @param source  The source of the message (class or component name)
     * @param message The message to log
     */
    public static void info(String source, String message) {
        log("INFO", source, message, null);
    }

    /**
     * Log an error message.
     *
     * @param source  The source of the message (class or component name)
     * @param message The error message to log
     */
    public static void error(String source, String message) {
        log("ERROR", source, message, null);
    }

    /**
     * Log an error message with an exception.
     *
     * @param source  The source of the message (class or component name)
     * @param message The error message to log
     * @param t       The exception to log
     */
    public static void error(String source, String message, Throwable t) {
        log("ERROR", source, message, t);
    }

    /**
     * Internal method to log messages with a specified level.
     *
     * @param level   The log level (INFO, WARN, ERROR, DEBUG)
     * @param source  The source of the message
     * @param message The message to log
     * @param t       An optional Throwable to log
     */
    private static synchronized void log(String level, String source, String message, Throwable t) {
        String timestamp = DATE_FORMAT.format(new Date());
        String logMessage = String.format("[%s] [%s] [%s] %s", timestamp, level, source, message);

        // Write to log file
        try (FileWriter fw = new FileWriter(LOG_FILE, true); PrintWriter pw = new PrintWriter(fw)) {

            pw.println(logMessage);

            // If there's an exception, print its stack trace to the log
            if (t != null) {
                pw.println("Exception details:");
                t.printStackTrace(pw);
                pw.println(); // Add an empty line after the stack trace
            }
        } catch (IOException e) {
            // Fall back to console if file logging fails
            System.err.println("Failed to write to log file: " + e.getMessage());
            System.err.println(logMessage);
            if (t != null) {
                t.printStackTrace();
            }
        }

        // Also print to console for development purposes
        System.out.println(logMessage);
        if (t != null) {
            t.printStackTrace(System.out);
        }
    }
}
