package com.flick.capabilities;

import com.flick.config.ConfigManager;
import org.openqa.selenium.remote.DesiredCapabilities;

public class CloudCapabilities {

    public static DesiredCapabilities getCapabilities(String platform) {
        DesiredCapabilities caps = new DesiredCapabilities();

        final boolean isIOS = "ios".equalsIgnoreCase(platform);

        // Zorunlu temel ayarlar
        caps.setCapability("platformName", isIOS ? "iOS" : "Android");
        caps.setCapability("appium:automationName", isIOS ? "XCUITest" : "UiAutomator2");

        // Genel davranışlar
        caps.setCapability("appium:noReset", true);
        caps.setCapability("appium:fullReset", false);
        caps.setCapability("appium:language", "en");
        caps.setCapability("appium:locale", "US");

        // iOS özel – izinler ve portlar
        if (isIOS) {
            caps.setCapability("appium:autoAcceptAlerts", true);
            caps.setCapability("appium:autoDismissAlerts", true);
            // Port çakışması olmaması için gw tabanlı offset
            caps.setCapability("appium:wdaLocalPort", ConfigManager.getPortOffset(41));
            caps.setCapability("appium:webkitDebugProxyPort", ConfigManager.getPortOffset(42));
        }

        // Momentum’da cihaz GW ile seçilecek: deviceName/udid boş kalmalı
        caps.setCapability("appium:deviceName", "");
        caps.setCapability("appium:udid", "");

        // App seçim mantığı: cloud path varsa 'app', yoksa bundleId ile başlat
        String appCloudPath = ConfigManager.getAppCloudPath();
        String bundleId     = ConfigManager.getAppBundleId();

        if (appCloudPath != null && !appCloudPath.isEmpty()) {
            caps.setCapability("appium:app", appCloudPath);
        } else if (bundleId != null && !bundleId.isEmpty()) {
            // Örn: TestFlight gibi cihazda kurulu app'i aç
            caps.setCapability("appium:bundleId", bundleId);
        }

        // MomentumSuite kimlik ve GW bilgileri
        caps.setCapability("momentum:options", ConfigManager.getMomentumOptions());

        return caps;
    }
}
