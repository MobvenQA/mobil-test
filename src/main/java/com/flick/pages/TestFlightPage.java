package com.flick.pages;

import com.flick.config.ConfigManager;
import com.flick.drivers.DriverFactory;
import com.flick.utils.LogLevel;
import com.flick.utils.LoggerHelper;
import com.flick.utils.ScreenshotHelper;
import com.flick.utils.TestUtils;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.ios.IOSDriver;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class TestFlightPage {

    private final IOSDriver driver;
    private final TestUtils utils;
    private final String appName;

    /**
     * DriverFactory ve ConfigManager üzerinden otomatik driver & appName yüklenir.
     */
    public TestFlightPage() {
        this.driver = (IOSDriver) DriverFactory.getDriver();
        this.utils  = new TestUtils(); // DriverFactory ile otomatik bağlanır
        this.appName = ConfigManager.getAppName(); // SuiteHooks appKey set ettiği için güvenilir
    }

    // Locators
    private final By allowOnce = AppiumBy.accessibilityId("Allow Once");
    private final By notNow = AppiumBy.accessibilityId("Not Now");
    private final By installBtn = AppiumBy.xpath(
            "//*[contains(@name,'Install') or contains(@name,'INSTALL') or contains(@label,'YÜKLE') or contains(@label,'Yükle')]"
    );
    private final By openBtn = AppiumBy.xpath(
            "//*[contains(@name,'Open') or contains(@name,'OPEN') or contains(@name,'Aç') or contains(@name,'AÇ')]"
    );
    private final By okBtn = AppiumBy.accessibilityId("Ok");

    // -------- İş Akışı Metodları --------

    public void removeAppPhones() {
        try {
            Thread.sleep(1500);
            List<By> editVariants = Arrays.asList(
                    AppiumBy.xpath("(//*[contains(@name,'Edit') or contains(@label,'Edit') or contains(@value,'Edit')])[2]"),
                    AppiumBy.xpath("(//*[contains(@name,'Edit') or contains(@label,'Edit') or contains(@value,'Edit')])[1]"),
                    AppiumBy.accessibilityId("Edit Button")
            );
            utils.safeClickOneOf(editVariants, 10);

            Thread.sleep(1500);
            List<By> removeVariants = Arrays.asList(
                    By.xpath("(//XCUIElementTypeImage[@name='minus.circle.fill'])[2]"),
                    By.xpath("(//XCUIElementTypeImage[@name='remove'])[2]"),
                    By.xpath("(//XCUIElementTypeCell)[6]"),
                    By.xpath("(//*[contains(@name,'iPhone ')])[2]")
            );
            utils.safeClickOneOf(removeVariants, 10);

            List<By> deleteVariants = Arrays.asList(
                    By.xpath("//XCUIElementTypeStaticText[@name='Delete']"),
                    AppiumBy.accessibilityId("Remove Button")
            );
            Thread.sleep(1500);
            utils.safeClickOneOf(deleteVariants, 10);

            utils.waitAndClick(By.xpath("//XCUIElementTypeOther[@name='Dismiss-Button']"), 5);

            List<By> doneVariants = Arrays.asList(
                    AppiumBy.accessibilityId("Done"),
                    AppiumBy.accessibilityId("Done Button"),
                    AppiumBy.xpath("(//XCUIElementTypeButton[@name='Done'])[2]"),
                    AppiumBy.xpath("(//XCUIElementTypeButton[@name='Done'])[1]")
            );
            utils.safeClickOneOf(doneVariants, 10);

            LoggerHelper.log(LogLevel.INFO, "✅ removeAppPhones işlemi tamamlandı.");
        } catch (Exception e) {
            LoggerHelper.log(LogLevel.ERROR, "❌ removeAppPhones işlemi başarısız: " + e.getMessage());
        }
    }

    @Step("openTestFlight")
    public void openTestFlight(String bundleId) {
        int maxRetries = 3;
        for (int retry = 1; retry <= maxRetries; retry++) {
            try {
                driver.terminateApp(bundleId);
                Thread.sleep(1000);
                driver.activateApp(bundleId);
                new WebDriverWait(driver, Duration.ofSeconds(20)).until(d -> d.getPageSource() != null);
                ScreenshotHelper.captureAndAttach(driver, "openTestFlight", LogLevel.INFO);
                return;
            } catch (Exception e) {
                LoggerHelper.log(LogLevel.WARN, "❌ Retry " + retry + " failed: " + e.getMessage());
                if (retry == maxRetries) throw new RuntimeException("openTestFlight failed", e);
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
        }
    }

    @Step("handlePermissions")
    public void handlePermissions() {
        if (utils.waitUntilVisible(allowOnce, 5)) utils.waitAndClick(allowOnce, 5);
        if (utils.waitUntilVisible(notNow, 5)) utils.waitAndClick(notNow, 5);
        if (utils.waitUntilVisible(okBtn, 5)) utils.waitAndClick(okBtn, 5);
    }

    @Step("searchAndTapApp")
    public void searchAndTapApp(String appNameParam) throws Exception {
        String targetAppName = appNameParam != null ? appNameParam : this.appName;
        By appLocator = AppiumBy.accessibilityId(targetAppName);
        int maxScrolls = 5, currentScroll = 0;
        TestUtils.SwipeDirection direction = TestUtils.SwipeDirection.UP;

        while (currentScroll < maxScrolls * 2) {
            if (utils.waitUntilVisible(appLocator, 2)) {
                utils.waitAndClick(appLocator, 5);
                LoggerHelper.log(LogLevel.INFO, "✅ App bulundu ve tıklandı: " + targetAppName);
                return;
            }
            utils.swipe(direction, 3);
            currentScroll++;
            if (currentScroll == maxScrolls) direction = TestUtils.SwipeDirection.DOWN;
        }

        ScreenshotHelper.captureAndAttach(driver, "App not found", LogLevel.ERROR);
        throw new Exception("❌ App not found: " + targetAppName);
    }

    @Step("uninstallAppIfPresent")
    public void uninstallAppIfPresent(String targetBundleId) {
        if (driver.isAppInstalled(targetBundleId)) {
            driver.removeApp(targetBundleId);
            LoggerHelper.log(LogLevel.WARN, "✅ App kaldırıldı: " + targetBundleId);
        }
    }

    @Step("clickInstall")
    public void clickInstall() {
        utils.waitAndClick(installBtn, 90);
        try {
            By editButton = AppiumBy.xpath("//*[contains(@name,'Edit') or contains(@label,'Edit') or contains(@value,'Edit')]");
            if (utils.waitUntilVisible(editButton, 5)) {
                removeAppPhones();
                utils.clickWithDelay(installBtn, 15,60);
            }
        } catch (Exception ignored) {}
    }

    @Step("waitForAndClickOpen")
    public void waitForAndClickOpen() throws Exception {
        if (utils.waitUntilVisible(openBtn, 240)) {
            utils.waitAndClick(openBtn, 2);
        } else {
            throw new Exception("❌ OPEN button not found.");
        }
    }

    @Step("verifyApp")
    public void verifyApp(String appNameParam) {
        String targetAppName = appNameParam != null ? appNameParam : this.appName;
        LoggerHelper.log(LogLevel.INFO, "Open App Verify...");
        utils.waitUntilVisible(AppiumBy.xpath("//*[contains(@name, '" + targetAppName + "')]"), 5);
    }

    @Step("performFullInstallFlow")
    public void tfPAppInstall(String bundleId, String appNameParam, String targetBundleId) throws Exception {
        LoggerHelper.log(LogLevel.INFO, "TestFlight full app install flow...");
        openTestFlight(bundleId);
        handlePermissions();
        searchAndTapApp(appNameParam);
        uninstallAppIfPresent(targetBundleId);
        clickInstall();
        waitForAndClickOpen();
        verifyApp(appNameParam);
    }
}
