package edu.upenn.cit5940.logging;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/***
 * Logger class for logging messages to a file.
 * Implements the Singleton pattern to ensure only one instance exists.
 */
public class Logger {

	//Singleton instance
    private static Logger instance;
    private PrintWriter writer;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_LOG_FILE = "tech_news_search.log";

    private Logger(String filePath) {
        try {
            this.writer = new PrintWriter(new FileWriter(filePath, true), true);
        } catch (IOException e) {
            this.writer = null;
        }
    }

    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger(DEFAULT_LOG_FILE);
        }
        return instance;
    }

    //allows a custom log file path
    public static synchronized Logger getInstance(String filePath) {
        if (instance == null) {
            //if passes null or an empty string, fallback to default
            String path = (filePath == null || filePath.trim().isEmpty()) ? DEFAULT_LOG_FILE : filePath;
            instance = new Logger(path);
        }
        return instance;
    }

    //logging method formatting: [YYYY-MM-DD HH:MM:SS] + level + Message
    public synchronized void log(String level, String message) {
        if (writer != null) {
            String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
            writer.printf("[%s] %s %s%n", timestamp, level.toUpperCase(), message);
        }
    }
    
    //Convenience methods for different log levels
    public void info(String message) {
        log("INFO", message);
    }

    public void error(String message) {
        log("ERROR", message);
    }

    //Finalize output stream upon application exit
    public synchronized void close() {
        if (writer != null) {
            writer.flush();
            writer.close();
            writer = null;
            instance = null; // Reset instance if closed
        }
    }
}