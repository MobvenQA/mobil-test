package hooks;

import com.flick.config.ConfigManager;
import com.flick.drivers.DriverFactory;
import com.flick.utils.LogLevel;
import com.flick.utils.LoggerHelper;
import io.qameta.allure.Allure;
import org.testng.annotations.*;

import java.io.IOException;

public class SuiteHooks {

    /**
     * Her cihaz (her <test>) başlamadan önce driver ve context hazırlanır.
     * Suite seviyesinde Allure'a environment ve setup bilgileri eklenir.
     */
    @Parameters({"environment", "platform", "appKey", "deviceIndex", "tags"})
    @BeforeTest(alwaysRun = true)
    public void setupPerTest(@Optional("cloud") String env,
                             @Optional("ios") String platform,
                             @Optional("Playdate") String appKey,
                             @Optional("0") String deviceIndex,
                             @Optional("") String tagsParam) throws Exception {

        ConfigManager.setContext(env.toLowerCase(), platform.toLowerCase(), Integer.parseInt(deviceIndex));
        ConfigManager.setAppKey(appKey);

        // Suite raporuna bilgi ekle
        Allure.step("=== Test Environment Setup ===");
        Allure.step("Environment: " + env);
        Allure.step("Platform: " + platform);
        Allure.step("AppKey: " + appKey);
        Allure.step("Device Index: " + deviceIndex);
        if (tagsParam != null && !tagsParam.isBlank()) {
            System.setProperty("cucumber.filter.tags", tagsParam);
            Allure.step("Tags Filter: " + tagsParam);
        }

        LoggerHelper.log(LogLevel.INFO, "========== Test Setup ==========");
        LoggerHelper.log(LogLevel.INFO, "Env=" + env + " Platform=" + platform +
                " AppKey=" + appKey + " DeviceIndex=" + deviceIndex);
        LoggerHelper.log(LogLevel.INFO, "Tags=" + System.getProperty("cucumber.filter.tags", "(none)"));
        LoggerHelper.log(LogLevel.INFO, "================================");

        // Driver'ı sadece henüz başlatılmamışsa başlat
        if (DriverFactory.getDriver() == null) {
            DriverFactory.initDriver();
            LoggerHelper.log(LogLevel.INFO, "Driver initialized successfully");
        } else {
            LoggerHelper.log(LogLevel.INFO, "Driver already initialized, skipping...");
        }
    }

    /**
     * Her cihaz testi bittiğinde driver kapatılır.
     * Suite raporuna teardown bilgisi eklenir.
     */
    @AfterTest(alwaysRun = true)
    public void teardownPerTest() throws IOException, InterruptedException {
        LoggerHelper.log(LogLevel.INFO, "========== Test Teardown ==========");
        Allure.step("Test Completed for Device Index: " + ConfigManager.getDeviceIndex());

        DriverFactory.quitDriver();

        // İsteğe bağlı Allure sonucu gönderme scripti
        ProcessBuilder pb = new ProcessBuilder("bash", "send_result.sh");
        pb.redirectErrorStream(true);
        Process process = pb.start();

        new Thread(() -> {
            try (var reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException ignored) {}
        }).start();

        boolean finished = process.waitFor(120, java.util.concurrent.TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            System.err.println("❌ Allure script timeout: 120 saniye içinde bitmedi.");
        } else if (process.exitValue() == 0) {
            System.out.println("✅ Allure raporu başarıyla gönderildi.");
        } else {
            System.err.println("❌ Allure script başarısız çıktı kodu: " + process.exitValue());
        }

        LoggerHelper.log(LogLevel.INFO, "===================================");
    }
}
