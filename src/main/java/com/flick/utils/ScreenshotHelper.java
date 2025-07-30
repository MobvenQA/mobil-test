package com.flick.utils;

import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
            Allure.addAttachment("[" + level + "] " + stepName,
                    "text/plain", new ByteArrayInputStream("WebDriver null".getBytes()), ".txt");
            return;
        }
        try {
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("[" + level + "] " + stepName,
                    "image/png", new ByteArrayInputStream(png), ".png");
        } catch (Exception e) {
            Allure.addAttachment("[" + level + "] " + stepName,
                    "text/plain", new ByteArrayInputStream(("Screenshot alınamadı: " + e.getMessage()).getBytes()), ".txt");
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
    public static void captureAndAttachCompressed(WebDriver driver, String stepName, LogLevel level,
                                                  float quality, int maxWidth) {
        if (driver == null) {
            Allure.addAttachment("[" + level + "] " + stepName,
                    "text/plain", new ByteArrayInputStream("WebDriver null".getBytes()), ".txt");
            return;
        }
        try {
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            BufferedImage img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(png));

            // Ölçekle (opsiyonel)
            if (maxWidth > 0 && img.getWidth() > maxWidth) {
                int newW = maxWidth;
                int newH = (int)((double) img.getHeight() * newW / img.getWidth());
                java.awt.Image tmp = img.getScaledInstance(newW, newH, java.awt.Image.SCALE_SMOOTH);
                BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g2 = scaled.createGraphics();
                g2.drawImage(tmp, 0, 0, null);
                g2.dispose();
                img = scaled;
            } else if (img.getType() != BufferedImage.TYPE_INT_RGB) {
                BufferedImage rgb = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g2 = rgb.createGraphics();
                g2.drawImage(img, 0, 0, java.awt.Color.WHITE, null); // şeffaflık -> beyaz
                g2.dispose();
                img = rgb;
            }

            // JPEG’e sıkıştır
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageWriter writer = javax.imageio.ImageIO.getImageWritersByFormatName("jpg").next();
            javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality); // 0.0–1.0 (öneri: 0.75)
            writer.setOutput(javax.imageio.ImageIO.createImageOutputStream(baos));
            writer.write(null, new javax.imageio.IIOImage(img, null, null), param);
            writer.dispose();

            Allure.addAttachment("[" + level + "] " + stepName,
                    "image/jpeg", new ByteArrayInputStream(baos.toByteArray()), ".jpg");
        } catch (Exception e) {
            Allure.addAttachment("[" + level + "] " + stepName,
                    "text/plain", new ByteArrayInputStream(("Screenshot alınamadı: " + e.getMessage()).getBytes()), ".txt");
        }
    }
    public static void captureAndAttachScaled(WebDriver driver, String stepName, LogLevel level, int maxWidth) {
        if (driver == null) {
            Allure.addAttachment("[" + level + "] " + stepName,
                    "text/plain", new ByteArrayInputStream("WebDriver null".getBytes()), ".txt");
            return;
        }
        try {
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            BufferedImage img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(png));

            // Genişliği sınırlayarak ölçekle
            if (maxWidth > 0 && img.getWidth() > maxWidth) {
                int newW = maxWidth;
                int newH = (int) ((double) img.getHeight() * newW / img.getWidth());
                java.awt.Image tmp = img.getScaledInstance(newW, newH, java.awt.Image.SCALE_SMOOTH);
                BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g2 = scaled.createGraphics();
                g2.drawImage(tmp, 0, 0, null);
                g2.dispose();
                img = scaled;
            }

            // PNG olarak ekle (daha kaliteli)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(img, "png", baos);

            Allure.addAttachment("[" + level + "] " + stepName,
                    "image/png", new ByteArrayInputStream(baos.toByteArray()), ".png");
        } catch (Exception e) {
            Allure.addAttachment("[" + level + "] " + stepName,
                    "text/plain", new ByteArrayInputStream(("Screenshot alınamadı: " + e.getMessage()).getBytes()), ".txt");
        }
    }

}
