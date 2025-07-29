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
import java.util.HashMap;
import java.util.Map;

public class DriverFactory {
    private static final ThreadLocal<AppiumDriver> driver = new ThreadLocal<>();

    public static void initDriver() throws MalformedURLException {
        String environment = ConfigManager.getEnvironment();
        String platform = ConfigManager.getPlatform();

        DesiredCapabilities caps = "cloud".equalsIgnoreCase(environment)
                ? CloudCapabilities.getCapabilities(platform)
                : LocalCapabilities.getCapabilities(platform);

        String appPath = System.getProperty("app.cloudPath", "");
        String localPath = System.getProperty("app.localPath", "");
        String bundleId = System.getProperty("app.bundleId", "");
        String targetBundleId = System.getProperty("app.targetBundleId", "");
        boolean isTestFlight = Boolean.parseBoolean(System.getProperty("app.isTestFlight", "false"));

        // App yükleme yolları öncelikli
        if ((appPath != null && !appPath.isBlank()) || (localPath != null && !localPath.isBlank())) {
            String selectedPath = "cloud".equalsIgnoreCase(environment) ? appPath : localPath;
            caps.setCapability("appium:app", selectedPath);
            System.out.println("[DriverFactory] App ile başlatılıyor: " + selectedPath);
        }
        // BundleId (TestFlight hariç)
        else if (bundleId != null && !bundleId.isBlank() && !"com.apple.TestFlight".equals(bundleId)) {
            caps.setCapability("appium:bundleId", bundleId);
            System.out.println("[DriverFactory] BundleID ile başlatılıyor: " + bundleId);
        }
        // TestFlight
        else if (isTestFlight || "com.apple.TestFlight".equals(bundleId)) {
            caps.setCapability("appium:bundleId", "com.apple.TestFlight");
            if (targetBundleId != null && !targetBundleId.isBlank()) {
                caps.setCapability("momentum:targetBundleId", targetBundleId);
            }
            System.out.println("[DriverFactory] TestFlight/boş cihaz başlatılıyor ("
                    + ("cloud".equalsIgnoreCase(environment) ? "Cloud" : "Local") + ").");
        } else {
            System.out.println("[DriverFactory] App belirtilmedi, cihaz boş başlatılıyor.");
        }

        // momentum:options yalnızca Cloud için
        if ("cloud".equalsIgnoreCase(environment)) {
            caps.setCapability("momentum:options", ConfigManager.getMomentumOptions().toMap());
        }

        // Capabilities log
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> capsMap = new HashMap<>(caps.asMap());
            Object momentumOptions = capsMap.get("momentum:options");
            if (momentumOptions instanceof Map) {
                capsMap.put("momentum:options", momentumOptions);
            } else if (momentumOptions != null) {
                capsMap.put("momentum:options", momentumOptions.toString());
            }
            System.out.println("\n========== Appium Desired Capabilities ==========");
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capsMap));
            System.out.println("=================================================\n");
        } catch (Exception e) {
            System.err.println("[WARN] Capabilities JSON log yazılamadı: " + e.getMessage());
        }

        String serverUrlStr = "cloud".equalsIgnoreCase(environment)
                ? ConfigManager.getCloudServerUrl()
                : "http://127.0.0.1:" + ConfigManager.getDeviceGw();

        URL serverUrl = new URL(serverUrlStr);
        if ("android".equalsIgnoreCase(platform)) {
            driver.set(new AndroidDriver(serverUrl, caps));
        } else if ("ios".equalsIgnoreCase(platform)) {
            driver.set(new IOSDriver(serverUrl, caps));
        } else {
            throw new IllegalArgumentException("Unsupported platform: " + platform);
        }

        System.out.println("[INFO] Appium driver başlatıldı (" + environment + " / " + platform +
                " @port " + ConfigManager.getDeviceGw() + ")");
    }

    public static AppiumDriver getDriver() {
        return driver.get();
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
            System.out.println("[INFO] Appium driver kapatıldı.");
        }
    }
}
