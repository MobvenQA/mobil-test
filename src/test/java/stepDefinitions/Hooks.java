package stepDefinitions;

import com.flick.config.ConfigManager;
import com.flick.drivers.DriverFactory;
import com.flick.utils.LogLevel;
import com.flick.utils.LoggerHelper;
import io.cucumber.java.*;

public class Hooks {

    @Before
    public void beforeScenario(Scenario scenario) {
        try {
            LoggerHelper.startTestLog(scenario.getName());
            LoggerHelper.log(LogLevel.INFO, "Scenario Başlatıldı: " + scenario.getName());

            // Eğer context yoksa, fallback ile ayarla
            if (ConfigManager.getAppKey() == null || ConfigManager.getAppKey().isEmpty()) {
                String env = System.getProperty("environment", "local");
                String platform = System.getProperty("platform", "ios");
                int deviceIndex = Integer.parseInt(System.getProperty("deviceIndex", "0"));
                String appKey = System.getProperty("appKey");
                if (appKey == null || appKey.isEmpty()) {
                    appKey = ConfigManager.getDefaultAppKey();
                }
                ConfigManager.setContext(env, platform, deviceIndex);
                ConfigManager.setAppKey(appKey);
            }
            // Eğer driver yoksa başlat
            if (DriverFactory.getDriver() == null) {
                try {
                    DriverFactory.initDriver();
                } catch (Exception e) {
                    throw new RuntimeException("Driver başlatılamadı!", e);
                }
            }

            LoggerHelper.log(LogLevel.INFO, "Env: " + ConfigManager.getEnvironment()
                    + " | Platform: " + ConfigManager.getPlatform()
                    + " | Device Index: " + ConfigManager.getDeviceIndex()
                    + " | AppKey: " + ConfigManager.getAppKey());

        } catch (Exception e) {
            LoggerHelper.log(LogLevel.ERROR, "Senaryo başlatılamadı: " + e.getMessage());
            throw new RuntimeException("Senaryo başlatma hatası", e);
        }
    }

    @After
    public void afterScenario(Scenario scenario) {
        LoggerHelper.log(LogLevel.INFO, "Senaryo Tamamlandı: " + scenario.getName() + " => " + scenario.getStatus());
        LoggerHelper.endTestLog(scenario.getName());
    }
}
