package stepDefinitions;

import com.flick.drivers.DriverFactory;
import com.flick.pages.TestFlightPage;
import com.flick.utils.LogLevel;
import com.flick.utils.LoggerHelper;
import com.flick.utils.ScreenshotHelper;
import io.cucumber.java.*;

public class Hooks {

    @Before
    public void beforeScenario(Scenario scenario) {
        LoggerHelper.startTestLog(scenario.getName());
        LoggerHelper.log(LogLevel.INFO, "Scenario START: " + scenario.getName());
        new TestFlightPage().handlePermissions();
    }

    @After
    public void afterScenario(Scenario scenario) {
        LoggerHelper.log(LogLevel.INFO,
                "Scenario END  : " + scenario.getName() + " => " + scenario.getStatus());

        if (scenario.isFailed() && DriverFactory.getDriver() != null) {
            ScreenshotHelper.captureAndAttach(
                    DriverFactory.getDriver(),
                    "FAILED: " + scenario.getName(),
                    LogLevel.ERROR
            );
        }
        LoggerHelper.endTestLog(scenario.getName());
        // Driver kapatma SuiteHooks’ta (suite-scope)
    }

    @BeforeStep
    public void beforeStep() {
        // Her adımda popup temizleme güvenli şekilde
        if (DriverFactory.getDriver() != null) {
            try {
                // TestFlightPage artık AppKey olmadan çağrılabilir (handlePermissions statik/metot)
                //new TestFlightPage().handlePermissions();
            } catch (Exception e) {
                LoggerHelper.log(LogLevel.WARN,
                        "[PopupDismiss] Hata (önemsiz): " + e.getMessage());
            }
        }
    }

    @AfterStep
    public void afterStep(Scenario scenario) {
        if (DriverFactory.getDriver() != null) {
            ScreenshotHelper.captureAndAttach(
                    DriverFactory.getDriver(),
                    "After step: " + scenario.getName(),
                    LogLevel.INFO
            );
        }
    }
}
