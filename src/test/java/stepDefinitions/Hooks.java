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

            // Güvenlik: appKey boşsa (örn. yanlış sırayla çalıştıysa) System prop’tan al
            if (ConfigManager.getAppKey() == null || ConfigManager.getAppKey().isEmpty()) {
                String fallback = System.getProperty("appKey", "");
                if (!fallback.isEmpty()) {
                    ConfigManager.setAppKey(fallback);
                }
            }

            LoggerHelper.log(LogLevel.INFO, "Env: " + ConfigManager.getEnvironment()
                    + " | Platform: " + ConfigManager.getPlatform()
                    + " | Device Index: " + ConfigManager.getDeviceIndex()
                    + " | AppKey: " + ConfigManager.getAppKey());

            DriverFactory.initDriver();
        } catch (Exception e) {
            LoggerHelper.log(LogLevel.ERROR, "Senaryo başlatılamadı: " + e.getMessage());
            throw new RuntimeException("Senaryo başlatma hatası", e);
        }
    }

    @After
    public void afterScenario(Scenario scenario) {
        LoggerHelper.log(LogLevel.INFO, "Senaryo Tamamlandı: " + scenario.getName() + " => " + scenario.getStatus());
        DriverFactory.quitDriver();
        LoggerHelper.endTestLog(scenario.getName());
    }
}
