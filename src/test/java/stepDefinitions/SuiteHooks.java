package stepDefinitions;

import com.flick.config.ConfigManager;
import com.flick.drivers.DriverFactory;
import com.flick.utils.LogLevel;
import com.flick.utils.LoggerHelper;
import org.testng.annotations.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SuiteHooks {

    /**
     * Her cihaz (her <test>) başlamadan önce driver ve context hazırlanır.
     * Bu sayede paralel testlerde her cihaz ayrı Appium session kullanır.
     */
    @Parameters({"environment", "platform", "appKey", "deviceIndex", "tags"})
    @BeforeTest(alwaysRun = true)
    public void setupPerTest(@Optional("cloud") String env,
                             @Optional("ios") String platform,
                             @Optional("Playdate") String appKey,
                             @Optional("0") String deviceIndex,
                             @Optional("") String tagsParam) throws Exception {
        // Context thread-bazlı ayarlanır
        ConfigManager.setContext(env.toLowerCase(), platform.toLowerCase(), Integer.parseInt(deviceIndex));
        ConfigManager.setAppKey(appKey);

        // Tag filtresi (Cucumber için)
        if (tagsParam != null && !tagsParam.isBlank()) {
            System.setProperty("cucumber.filter.tags", tagsParam);
        }

        LoggerHelper.log(LogLevel.INFO, "========== Test Setup ==========");
        LoggerHelper.log(LogLevel.INFO, "Env=" + env + " Platform=" + platform +
                " AppKey=" + appKey + " DeviceIndex=" + deviceIndex);
        LoggerHelper.log(LogLevel.INFO, "Tags=" + System.getProperty("cucumber.filter.tags", "(none)"));
        LoggerHelper.log(LogLevel.INFO, "================================");

        // Her cihaz için driver başlat
        DriverFactory.initDriver();
    }

    /**
     * Her cihaz testi bittiğinde driver kapatılır.
     * ThreadLocal sayesinde sadece kendi thread’inin driver’ı kapanır.
     */
    @AfterTest(alwaysRun = true)
    public void teardownPerTest() throws IOException, InterruptedException {
        LoggerHelper.log(LogLevel.INFO, "========== Test Teardown ==========");

        // Önce driver kapat (allure dosyaları etkilenmiyorsa)
        DriverFactory.quitDriver();

        LoggerHelper.log(LogLevel.INFO, "Allure raporu gönderiliyor...");

        ProcessBuilder pb = new ProcessBuilder("bash", "send_result.sh");
        pb.redirectErrorStream(true); // stdout + stderr birlikte gelsin
        Process process = pb.start();

        // Script'ten gelen logları anlık yazdır
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
