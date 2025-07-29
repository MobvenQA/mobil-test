package stepDefinitions;

import com.flick.config.ConfigManager;
import com.flick.pages.TestFlightPage;
import com.flick.utils.LoggerHelper;
import com.flick.utils.LogLevel;
import io.cucumber.java.en.*;

public class TestFlightSteps {

    private final TestFlightPage testFlightPage;

    public TestFlightSteps() {
        this.testFlightPage = new TestFlightPage(); // Driver ve appName TestFlightPage içinde alınır
    }

    @Given("TestFlight açılır ve {string} terminate & activate edilir")
    public void testflightOpen(String bundleId) {
        LoggerHelper.log(LogLevel.INFO, "TestFlight açılıyor ve terminate+activate yapılıyor...");
        testFlightPage.openTestFlight(bundleId);
        testFlightPage.handlePermissions();
    }

    @When("{string} uygulaması TestFlight'ta aranır ve bulunursa tıklanır")
    public void searchAndTap(String appKey) throws Exception {
        LoggerHelper.log(LogLevel.INFO, "TestFlight içinde uygulama aranıyor: " + appKey);

        // ConfigManager'dan appName al (SuiteHooks appKey set ettiği için güvenilir)
        String appName = ConfigManager.getAppName();
        testFlightPage.searchAndTapApp(appName != null ? appName : appKey);
    }

    @And("Hedef uygulama {string} yüklü ise kaldırılır")
    public void uninstallIfPresent(String targetBundleId) {
        LoggerHelper.log(LogLevel.WARN, "Mevcut kurulu uygulama kaldırılıyor (varsa): " + targetBundleId);
        testFlightPage.uninstallAppIfPresent(targetBundleId);
    }

    @And("Install butonuna basılır ve yükleme tamamlanır")
    public void installApp() {
        LoggerHelper.log(LogLevel.INFO, "TestFlight Install akışı başlıyor...");
        testFlightPage.clickInstall();
    }

    @And("OPEN butonuna basılarak uygulama açılır")
    public void openApp() throws Exception {

        LoggerHelper.log(LogLevel.INFO, "OPEN butonu bekleniyor ve tıklanıyor...");
        testFlightPage.waitForAndClickOpen();
    }

    @Then("{string} uygulamasının başarıyla açıldığı doğrulanır")
    public void verifyAppOpened(String appKey) {
        LoggerHelper.log(LogLevel.INFO, "Uygulama açılış doğrulaması yapılıyor: " + appKey);
        String appName = ConfigManager.getAppName();
        testFlightPage.verifyApp(appName != null ? appName : appKey);
        LoggerHelper.log(LogLevel.INFO, "Doğrulama tamamlandı: " + appKey);
    }
}
