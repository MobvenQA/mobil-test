package stepDefinitions;

import com.flick.config.ConfigManager;
import com.flick.drivers.DriverFactory;
import com.flick.utils.LogLevel;
import com.flick.utils.LoggerHelper;
import org.testng.annotations.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SuiteHooks {

    @Parameters({"environment", "platform", "appKey", "deviceIndex"})
    @BeforeTest(alwaysRun = true)
    public void beforeEachTest(@Optional("local") String envParam,
                               @Optional("ios")   String platformParam,
                               @Optional("sampleApp") String appKeyParam,
                               @Optional("0")     String deviceIndexParam) throws MalformedURLException {

        // CLI (-D...) varsa onu kullan; yoksa XML parametreleri
        String environment = System.getProperty("environment", envParam).toLowerCase();
        String platform    = System.getProperty("platform", platformParam).toLowerCase();
        String appKey      = System.getProperty("appKey", appKeyParam);
        int deviceIndex    = Integer.parseInt(System.getProperty("deviceIndex", deviceIndexParam));

        // Her TEST (ve thread) için context + appKey set edilir
        ConfigManager.setContext(environment, platform, deviceIndex);
        ConfigManager.setAppKey(appKey);

        LoggerHelper.log(LogLevel.INFO, "========== Test Setup (Per-Test) ==========");
        LoggerHelper.log(LogLevel.INFO, "Environment: " + environment);
        LoggerHelper.log(LogLevel.INFO, "Platform   : " + platform);
        LoggerHelper.log(LogLevel.INFO, "AppKey     : " + appKey);
        LoggerHelper.log(LogLevel.INFO, "DeviceIndex: " + deviceIndex);
        LoggerHelper.log(LogLevel.INFO, "===========================================");
        DriverFactory.initDriver();
    }

    @AfterSuite(alwaysRun = true)
    public void afterAllTests() throws IOException, InterruptedException {
        LoggerHelper.log(LogLevel.INFO, "========== Global Suite Teardown ==========");
        LoggerHelper.log(LogLevel.INFO, "Tüm raporlar birleştiriliyor, cleanup başlatıldı...");
        LoggerHelper.log(LogLevel.INFO, "===========================================");
        ProcessBuilder pb = new ProcessBuilder("bash", "send_result.sh");
        pb.inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            System.out.println("✅ Allure raporu başarıyla gönderildi.");
        } else {
            System.err.println("❌ Allure script başarısız çıktı kodu: " + exitCode);
        }

        DriverFactory.quitDriver();
        Files.list(Paths.get("allure-results")).forEach(System.out::println);
    }
}
