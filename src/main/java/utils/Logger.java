package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_FILE = "bazaar_log.txt"; // Path to the log file

    // Date and time formatter
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS");

    // Method to log messages with a timestamp
    public static void log(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = timestamp + " " + message;

        // Print to console
        System.out.println(logMessage);

        // Append the message to the log file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(logMessage);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }
}