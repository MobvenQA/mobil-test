package hooks;

import com.flick.drivers.DriverFactory;
import com.flick.pages.TestFlightPage;
import com.flick.utils.LogLevel;
import com.flick.utils.LoggerHelper;
import com.flick.utils.ScreenshotHelper;
import io.cucumber.java.*;
import io.qameta.allure.Allure;

public class Hooks {

    @Before
    public void beforeScenario(Scenario scenario) {
        LoggerHelper.startTestLog(scenario.getName());
        LoggerHelper.log(LogLevel.INFO, "Scenario START: " + scenario.getName());
        Allure.step("Scenario START: " + scenario.getName());

        // Driver'ın hazır olduğundan emin ol
        if (DriverFactory.getDriver() == null) {
            LoggerHelper.log(LogLevel.WARN, "Driver not initialized, attempting to initialize...");
            try {
                DriverFactory.initDriver();
            } catch (Exception e) {
                LoggerHelper.log(LogLevel.ERROR, "Failed to initialize driver: " + e.getMessage());
            }
        }

        // TestFlight izin ekranlarını senaryo başında temizle
        if (DriverFactory.getDriver() != null) {
            new TestFlightPage().handlePermissions();
        }
    }

    @After
    public void afterScenario(Scenario scenario) {
        LoggerHelper.log(LogLevel.INFO,
                "Scenario END: " + scenario.getName() + " => " + scenario.getStatus());
        Allure.step("Scenario END: " + scenario.getName() + " => " + scenario.getStatus());

        if (scenario.isFailed() && DriverFactory.getDriver() != null) {
            ScreenshotHelper.captureAndAttachScaled(
                    DriverFactory.getDriver(),
                    scenario.getName(),
                    LogLevel.ERROR,
                    600
            );
        }

        LoggerHelper.endTestLog(scenario.getName());
    }

    @BeforeStep
    public void beforeStep() {
        if (DriverFactory.getDriver() != null) {
            try {
                // Popup dismiss işlemleri
                // new TestFlightPage().handlePermissions();
            } catch (Exception e) {
                LoggerHelper.log(LogLevel.WARN, "[PopupDismiss] Hata (önemsiz): " + e.getMessage());
            }
        }
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        if (DriverFactory.getDriver() != null) {
            Allure.step("After step: " + scenario.getName());
            ScreenshotHelper.captureAndAttachScaled(
                    DriverFactory.getDriver(),
                    scenario.getName(),
                    LogLevel.INFO,
                    600
            );
        }
    }
}
