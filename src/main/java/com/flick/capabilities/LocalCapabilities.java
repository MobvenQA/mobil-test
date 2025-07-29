package com.flick.capabilities;

import com.flick.config.ConfigManager;
import org.openqa.selenium.remote.DesiredCapabilities;

public class LocalCapabilities {

    public static DesiredCapabilities getCapabilities(String platform) {
        DesiredCapabilities caps = new DesiredCapabilities();

        caps.setCapability("platformName", platform);
        caps.setCapability("appium:automationName",
                platform.equalsIgnoreCase("ios") ? "XCUITest" : "UiAutomator2");
        caps.setCapability("appium:noReset", true);
        caps.setCapability("appium:fullReset", false);
        caps.setCapability("appium:language", "en");
        caps.setCapability("appium:locale", "US");

        // Local cihaz bilgileri
        caps.setCapability("appium:deviceName", ConfigManager.getDeviceName());
        caps.setCapability("appium:udid", ConfigManager.getDeviceUdid());
        caps.setCapability("appium:platformVersion", ConfigManager.getDeviceVersion());

        // Port çakışmasını önlemek için gw + 4000 offset kullan
        caps.setCapability("appium:wdaLocalPort", ConfigManager.getPortOffset(41));
        caps.setCapability("appium:webkitDebugProxyPort", ConfigManager.getPortOffset(42));

        // Local app path
        String appLocalPath = ConfigManager.getAppLocalPath();
        if (appLocalPath != null && !appLocalPath.isEmpty()) {
            caps.setCapability("appium:app", appLocalPath);
        }

        return caps;
    }
}
