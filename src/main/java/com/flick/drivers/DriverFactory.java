package com.flick.drivers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flick.capabilities.CloudCapabilities;
import com.flick.capabilities.LocalCapabilities;
import com.flick.config.ConfigManager;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;

public class DriverFactory {
    private static final ThreadLocal<AppiumDriver> driver = new ThreadLocal<>();

    /**
     * Appium driver başlatır (IPA/APK, bundleId veya sadece cihaz bağlantısı ile).
     */
    public static void initDriver() throws MalformedURLException {
        String environment = ConfigManager.getEnvironment();
        String platform = ConfigManager.getPlatform();
        int deviceIndex = ConfigManager.getDeviceIndex();

        // Local/Cloud için capability seti
        DesiredCapabilities caps = "cloud".equalsIgnoreCase(environment)
                ? CloudCapabilities.getCapabilities(platform)
                : LocalCapabilities.getCapabilities(platform);

        // Session başlatma opsiyonları:
        String appPath = System.getProperty("app.cloudPath", "");
        String localPath = System.getProperty("app.localPath", "");
        String bundleId = System.getProperty("app.bundleId", "");
        boolean isTestFlight = Boolean.parseBoolean(System.getProperty("app.isTestFlight", "false"));

        // 1) IPA/APK yolu ile (cloud veya local)
        if ((appPath != null && !appPath.trim().isEmpty()) || (localPath != null && !localPath.trim().isEmpty())) {
            String selectedPath = "cloud".equalsIgnoreCase(environment) ? appPath : localPath;
            caps.setCapability("appium:app", selectedPath);
            System.out.println("[DriverFactory] App ile session başlatılıyor: " + selectedPath);
        }
        // 2) BundleID ile (cihazda yüklü uygulama)
        else if (bundleId != null && !bundleId.trim().isEmpty() && !"com.apple.TestFlight".equals(bundleId)) {
            caps.setCapability("appium:bundleId", bundleId);
            System.out.println("[DriverFactory] BundleID ile session başlatılıyor: " + bundleId);
        }
        // 3) TestFlight ya da hiç app olmadan (cihaz sadece açılır)
        else if (isTestFlight || (bundleId != null && "com.apple.TestFlight".equals(bundleId))) {
            caps.setCapability("appium:bundleId", "com.apple.TestFlight");
            if (System.getProperty("app.targetBundleId") != null) {
                caps.setCapability("momentum:targetBundleId", System.getProperty("app.targetBundleId"));
            }
            System.out.println("[DriverFactory] TestFlight ile veya app olmadan session başlatılıyor (cihaz açık).");
        } else {
            System.out.println("[DriverFactory] Uygulama belirtilmedi, cihaz boş şekilde başlatılıyor.");
        }

        // Capabilities loglama (debug amaçlı)
        try {
            ObjectMapper mapper = new ObjectMapper();
            System.out.println("\n========== Appium Desired Capabilities ==========");
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(caps.asMap()));
            System.out.println("=================================================\n");
        } catch (Exception e) {
            System.err.println("[WARN] Capabilities JSON log yazılamadı: " + e.getMessage());
        }

        // Server URL (cloud vs local)
        URL serverUrl = "cloud".equalsIgnoreCase(environment)
                ? new URL("https://console.momentumsuite.com/gateway/wd/hub")
                : new URL("http://127.0.0.1:4723/");

        // Driver başlat
        if ("android".equalsIgnoreCase(platform)) {
            driver.set(new AndroidDriver(serverUrl, caps));
        } else if ("ios".equalsIgnoreCase(platform)) {
            driver.set(new IOSDriver(serverUrl, caps));
        } else {
            throw new IllegalArgumentException("Unsupported platform: " + platform);
        }

        System.out.println("[INFO] Appium driver başarıyla başlatıldı (" + environment + " / " + platform + ")");
    }

    /** Thread bazlı driver döner */
    public static AppiumDriver getDriver() {
        return driver.get();
    }

    /** Thread bazlı driver kapatır */
    public static void quitDriver() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
            System.out.println("[INFO] Appium driver kapatıldı.");
        }
    }
}
