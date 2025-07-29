package com.flick.utils;

import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

/**
 * Modern Screenshot Helper
 * - Allure entegrasyonu öncelikli.
 * - İstenirse dosyaya kaydetme opsiyonu mevcut.
 * - Kod tekrarları azaltıldı.
 */
public class ScreenshotHelper {

    /**
     * Ekran görüntüsünü Allure raporuna ekler.
     * @param driver WebDriver
     * @param stepName Adım adı
     * @param level Log seviyesi (INFO, ERROR vs.)
     */
    public static void captureAndAttach(WebDriver driver, String stepName, LogLevel level) {
        if (driver == null) {
            Allure.addAttachment("[" + level + "] " + stepName, "❌ WebDriver null, screenshot alınamadı.");
            return;
        }
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("[" + level + "] " + stepName, new ByteArrayInputStream(screenshot));
        } catch (Exception e) {
            Allure.addAttachment("[" + level + "] " + stepName, "❌ Screenshot alınamadı: " + e.getMessage());
        }
    }

    /**
     * Ekran görüntüsünü Base64 olarak Allure’a ekler.
     * (Genellikle video ya da inline görseller için kullanılır)
     */
    public static void attachBase64ToAllure(WebDriver driver, String stepName) {
        if (driver == null) {
            Allure.addAttachment(stepName, "❌ WebDriver null, Base64 eklenemedi.");
            return;
        }
        try {
            String base64Screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
            if (base64Screenshot != null && !base64Screenshot.isEmpty()) {
                byte[] decoded = Base64.getDecoder().decode(base64Screenshot);
                Allure.addAttachment(stepName, "image/png", new ByteArrayInputStream(decoded), ".png");
            }
        } catch (Exception e) {
            Allure.addAttachment(stepName, "❌ Base64 ekran görüntüsü hatası: " + e.getMessage());
        }
    }

    /**
     * Debug veya lokal analiz için ekran görüntüsünü diske kaydeder (opsiyonel).
     * Varsayılan olarak "screenshots" klasörüne kaydeder.
     */
    public static void captureAndSave(WebDriver driver, String testName) {
        if (driver == null) {
            System.err.println("❌ WebDriver null, dosyaya screenshot kaydedilemedi.");
            return;
        }
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = "screenshots/" + testName + "_" + timestamp + ".png";

        try {
            byte[] imageBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            File file = new File(filename);
            file.getParentFile().mkdirs(); // "screenshots" klasörü yoksa oluştur
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(imageBytes);
            }
            System.out.println("📸 Screenshot kaydedildi: " + filename);
        } catch (Exception e) {
            System.err.println("❌ Screenshot dosyaya yazılamadı: " + e.getMessage());
        }
    }

    /**
     * Basit log mesajını Allure’a ekler (screenshot olmadan).
     */
    public static void logToAllure(String message, LogLevel level) {
        Allure.addAttachment("[" + level + "] Log", message);
    }
}
