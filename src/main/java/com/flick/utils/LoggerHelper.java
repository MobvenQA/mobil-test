package com.flick.utils;

import io.qameta.allure.Allure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerHelper {

    private static final String LOG_BASE_DIR = "logs";
    private static final ThreadLocal<String> logFileName = new ThreadLocal<>();

    /**
     * Her test başında çağrılır. Teste özel log dosyası başlatır.
     */
    public static void startTestLog(String testName) {
        String safeName = testName.replaceAll("[^a-zA-Z0-9-_]", "_"); // Dosya ismi için güvenli
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String filePath = LOG_BASE_DIR + File.separator + safeName + "_" + timestamp + ".log";
        logFileName.set(filePath);
        log(LogLevel.INFO, "===== START TEST: " + testName + " =====");
    }

    /**
     * Test bitince çağrılır. ThreadLocal temizlenir.
     */
    public static void endTestLog(String testName) {
        log(LogLevel.INFO, "===== END TEST: " + testName + " =====");
        logFileName.remove();
    }

    /**
     * Hem Allure, hem dosya hem konsol log’u yönetir.
     */
    public static void log(LogLevel level, String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String thread = Thread.currentThread().getName();
        String fullMessage = String.format("[%s] [%s] [%s] %s", timestamp, thread, level.name(), message);

        // Konsola yaz (renkli/ikonlu istenirse eklenebilir)
        if (level == LogLevel.ERROR) {
            System.err.println(fullMessage);
        } else {
            System.out.println(fullMessage);
        }

        // Allure’a log (ikonlu log seviyesiyle)
        String allurePrefix = getAllurePrefix(level);
        Allure.addAttachment(allurePrefix + " " + message, message);

        // Dosyaya yaz
        writeToFile(fullMessage);
    }

    private static String getAllurePrefix(LogLevel level) {
        switch (level) {
            case INFO: return "ℹ️ INFO";
            case WARN: return "⚠️ WARN";
            case ERROR: return "❌ ERROR";
            case DEBUG: return "🐞 DEBUG";
            default: return level.name();
        }
    }

    private static void writeToFile(String message) {
        try {
            String path = logFileName.get();
            if (path == null) return;

            File file = new File(path);
            file.getParentFile().mkdirs();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write(message + System.lineSeparator());
            }
        } catch (IOException e) {
            System.err.println("[LOGGER ERROR] Failed to write log: " + e.getMessage());
        }
    }
}
