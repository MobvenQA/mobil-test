package com.flick.utils;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;

import java.util.concurrent.TimeUnit;

public class TwoFactorHandler {

    /**
     * Handles Two-Factor Authentication flow when detected.
     * Generic, reusable logic that can be triggered from anywhere in the test flow.
     */
    public static void handleIfVisible(AppiumDriver driver) {
        try {
            if (driver == null) {
                System.err.println("❌ TwoFactorHandler: Driver nesnesi null.");
                return;
            }
            By twoFAHeader = AppiumBy.xpath("//*[contains(@name, 'Two-Factor Authentication') or contains(@label, 'Two-Factor Authentication') or contains(@value, 'Two-Factor Authentication') or contains(@name, 'Verification Code')]");
            if (driver.findElements(twoFAHeader).isEmpty()) {
                System.out.println("ℹ️ Two-Factor ekranı çıkmadı, işlem yapılmadı.");
                return;
            }
            System.out.println("🔐 Two-Factor ekranı algılandı, işlem başlatılıyor...");
            try {
                driver.findElement(By.xpath("//XCUIElementTypeStaticText[@name='Did not receive a code?']")).click();
                System.out.println("✅ Did not receive a code? Tıklandı" );
                ScreenshotHelper.captureAndAttach(driver, "Did not receive a code? Tıklandı", LogLevel.INFO);

                TimeUnit.SECONDS.sleep(1);

                driver.findElement(AppiumBy.accessibilityId("Cannot Get to My Devices")).click();
                System.out.println("✅ Cannot Get to My Devices Tıklandı" );
                ScreenshotHelper.captureAndAttach(driver, "Cannot Get to My Devices Tıklandı", LogLevel.INFO);

                TimeUnit.MILLISECONDS.sleep(15000);

                driver.findElement(AppiumBy.accessibilityId("Text the Code")).click();
                System.out.println("✅ Text the Code Tıklandı" );
                ScreenshotHelper.captureAndAttach(driver, "Text the Code Tıklandı", LogLevel.INFO);

                TimeUnit.MILLISECONDS.sleep(15000);
                System.out.println("📨 Kod gönderim adımları tamamlandı.");
            } catch (Exception e) {
                System.err.println("❌ UI adımlarında hata: " + e.getMessage());
                return;
            }
            String code = null;
            try {
                Thread.sleep(3000);
                code = SlackReader.getLatestAppleCodeWithin(30);
            } catch (Exception ex) {
                ex.printStackTrace(); // detaylı bilgi
                System.err.println("❌ SlackReader'dan kod alınamadı: " + ex.getMessage());
            }
            if (code == null) {
                System.err.println("❌ SlackReader dönen kod null, kod mesajı Slack'te gelmemiş olabilir.");
            }else if (code != null && code.length() == 6) {
                System.out.println("✅ Slack üzerinden 2FA kodu alındı: " + code);

                try {
                    driver.findElement(AppiumBy.xpath("//*[contains(@type,'XCUIElementTypeSecureTextField')]")).sendKeys(code);
                    ScreenshotHelper.captureAndAttach(driver, " ****Kod başarıyla girildi", LogLevel.INFO);

                    System.out.println("🔐 Kod başarıyla girildi.");

                } catch (Exception e) {
                    System.err.println("❌ Kod girilirken hata oluştu: " + e.getMessage());
                }
            } else {
                System.err.println("❌ Kod alınamadı veya geçersiz.");
            }


        } catch (Exception ex) {
            System.err.println("❌ handleIfVisible çalıştırılamadı: " + ex.getMessage());
        }
    }
}
