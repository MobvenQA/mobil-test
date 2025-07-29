package com.flick.utils;

public enum LogLevel {
    INFO("\u001B[32m"),    // Yeşil
    WARN("\u001B[33m"),    // Sarı
    ERROR("\u001B[31m"),   // Kırmızı
    DEBUG("\u001B[34m");   // Mavi

    private final String colorCode;
    private static final String RESET = "\u001B[0m";

    LogLevel(String colorCode) {
        this.colorCode = colorCode;
    }

    /**
     * Konsol için renkli çıktı (ANSI destekli terminallerde).
     * LoggerHelper timestamp ve thread ekleyeceği için burada sadece seviye + mesaj var.
     */
    public void print(String message) {
        System.out.println(colorCode + "[" + this.name() + "] " + message + RESET);
    }

    public void printErr(String message) {
        System.err.println(colorCode + "[" + this.name() + "] " + message + RESET);
    }

    /**
     * ANSI desteklemeyen durumlarda (ör. Allure) kullanılabilecek sade metin formatı.
     */
    public String plain(String message) {
        return "[" + this.name() + "] " + message;
    }

    /**
     * Renk kodlarıyla string döndürür (konsol için).
     */
    public String format(String message) {
        return colorCode + "[" + this.name() + "] " + message + RESET;
    }
}
