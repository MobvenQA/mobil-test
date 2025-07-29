package com.flick.pages;

import com.flick.drivers.DriverFactory;
import com.flick.utils.LogLevel;
import com.flick.utils.LoggerHelper;
import com.flick.utils.ScreenshotHelper;
import com.flick.utils.TestUtils;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class OnboardingPage {

    private final IOSDriver driver;
    private final TestUtils utils;

    public OnboardingPage() {
        this.driver = (IOSDriver) DriverFactory.getDriver();
        this.utils  = new TestUtils(); // DriverFactory'den alan no-arg ctor'un kalsın
    }

    // ===================== Locators (iOS) =====================
    private static final By APP_ICON               = AppiumBy.accessibilityId("Playdate - AI for Kids");
    private static final By OPEN_BUTTON            = AppiumBy.accessibilityId("Open");

    private static final By SIGNUP_APPLE           = AppiumBy.xpath("//XCUIElementTypeStaticText[@name='SIGN UP / LOGIN WITH APPLE']");
    private static final By SIGN_IN_WITH_PASSWORD  = AppiumBy.accessibilityId("Sign In with Password");
    private static final By SIGN_IN_WITH_PASSWORD2 = AppiumBy.xpath("//*[contains(@name,'Sign In with Password') or contains(@value,'Sign In with Password') or contains(@label,'Sign In with Password')]");

    private static final By CONTINUE_PASSWORD      = AppiumBy.accessibilityId("Continue with Password");
    private static final By CONTINUE_PASSWORD2     = AppiumBy.xpath("//*[contains(@name,'Continue with Password') or contains(@value,'Continue with Password') or contains(@label,'Continue with Password')]");

    private static final By SIGNIN_MANUALLY        = AppiumBy.accessibilityId("Sign in Manually");
    private static final By SIGNIN_MANUALLY2       = AppiumBy.xpath("//*[contains(@name,'Sign in Manually') or contains(@value,'Sign in Manually') or contains(@label,'Sign in Manually')]");

    private static final By START_FREE_TRIAL       = AppiumBy.xpath("//*[contains(@name,'Start Free Trial') or contains(@value,'Start Free Trial') or contains(@label,'Start Free Trial')]");
    private static final By START_FREE_TRIAL2      = AppiumBy.accessibilityId("Start Free Trial");

    private static final By NEXT_BUTTON            = AppiumBy.accessibilityId("Next");
    private static final By NEXT_BUTTON2           = AppiumBy.accessibilityId("NEXT");
    private static final By NEXT_BUTTON3           = AppiumBy.xpath("(//XCUIElementTypeButton[@name='NEXT'])[1]");

    private static final By PASSWORD_FIELD         = AppiumBy.xpath("//*[contains(@type,'XCUIElementTypeSecureTextField')]");
    private static final By SIGN_IN_BTN            = AppiumBy.xpath("//XCUIElementTypeButton[@name='Sign In']");
    private static final By SUBSCRIBE_BTN          = AppiumBy.accessibilityId("Subscribe");

    // Öncelikli buton listesi (ilk görünen tıklanır)
    private static final List<By> PRIMARY_BUTTONS = Arrays.asList(
            SIGNUP_APPLE,
            START_FREE_TRIAL,
            START_FREE_TRIAL2,
            SIGN_IN_WITH_PASSWORD,
            SIGN_IN_WITH_PASSWORD2,
            CONTINUE_PASSWORD,
            CONTINUE_PASSWORD2,
            SIGNIN_MANUALLY,
            SIGNIN_MANUALLY2
    );

    // "Next" varyantları
    private static final List<By> NEXT_VARIANTS = Arrays.asList(
            NEXT_BUTTON, NEXT_BUTTON2, NEXT_BUTTON3
    );

    // ===================== Flows =====================

    /** Intro carousel'de "NEXT" düğmelerine en fazla maxClicks kadar basar. */
    public void tapNextUpTo(int maxClicks) {
        int clickCount = 0;

        while (clickCount < maxClicks) {
            boolean clicked = false;

            for (By locator : NEXT_VARIANTS) {
                try {
                    if (utils.waitUntilVisible(locator, 1)) {
                        utils.waitAndClick(locator, 3);
                        LoggerHelper.log(LogLevel.INFO, "✅ NEXT tıklandı: " + locator);
                        ScreenshotHelper.captureAndAttach(driver, "NEXT click", LogLevel.INFO);
                        clicked = true;
                        clickCount++;
                        Thread.sleep(800);
                        break;
                    }
                } catch (Exception ignored) {
                    // görünmedi/geçmedi -> bir sonraki varyant
                }
            }

            if (!clicked) {
                LoggerHelper.log(LogLevel.WARN, "Hiçbir NEXT butonu görünür değil, döngüden çıkılıyor.");
                break;
            }
        }

        LoggerHelper.log(LogLevel.INFO, "NEXT tıklama işlemi tamamlandı. Toplam tıklama: " + clickCount);
    }

    /** Intro/landing swipe akışı (soldan sağa 3 kez, ardından yukarı 2 kez). */
    public void doIntroSwipes() {
        try {
            utils.swipe(TestUtils.SwipeDirection.LEFT, 1);
            utils.swipe(TestUtils.SwipeDirection.LEFT, 1);
            utils.swipe(TestUtils.SwipeDirection.LEFT, 1);

            utils.swipe(TestUtils.SwipeDirection.UP, 1);
            utils.swipe(TestUtils.SwipeDirection.UP, 1);

            ScreenshotHelper.captureAndAttach(driver, "Intro Swipes", LogLevel.INFO);
        } catch (Exception e) {
            LoggerHelper.log(LogLevel.WARN, "Intro swipes sırasında uyarı: " + e.getMessage());
        }
    }

    /**
     * Verilen buton listesinde 2 tur arayıp, ilk görünen butona tıklar.
     * Bulup tıklarsa true döner.
     */
    public int clickUpToThreePrimaryButtons() {
        int clickCount = 0;

        for (int round = 1; round <= 3; round++) {
            LoggerHelper.log(LogLevel.DEBUG, "Buton araması " + round + ". tur");

            boolean clickedThisRound = false;

            for (By selector : PRIMARY_BUTTONS) {
                try {
                    if (utils.waitUntilVisible(selector, 1)) {
                        LoggerHelper.log(LogLevel.INFO, "Buton bulundu ve tıklanıyor: " + selector);
                        utils.swipe(TestUtils.SwipeDirection.UP, 1); // gerekirse scroll
                        ScreenshotHelper.captureAndAttach(driver, "Before Click: " + selector, LogLevel.INFO);
                        utils.waitAndClick(selector, 5);
                        Thread.sleep(1500);
                        ScreenshotHelper.captureAndAttach(driver, "After Click: " + selector, LogLevel.INFO);

                        clickCount++;
                        clickedThisRound = true;
                        break; // Bu turda bir buton bulundu, diğer locator’lara bakmaya gerek yok
                    }
                } catch (Exception ex) {
                    LoggerHelper.log(LogLevel.DEBUG,
                            "Buton bulunamadı/kliklenemedi: " + selector + " | " + ex.getMessage());
                }
            }

            if (!clickedThisRound) {
                LoggerHelper.log(LogLevel.DEBUG, "Bu turda tıklanacak buton bulunamadı.");
            }

            // 3 buton tıklandıysa döngüden çık
            if (clickCount >= 3) break;

            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
        }

        LoggerHelper.log(LogLevel.INFO, "Toplam tıklanan buton sayısı: " + clickCount);
        return clickCount;
    }

    /** Subscribe varsa onu çalıştırır, yoksa şifre ile Sign In akışını dener. */
    public void subscribeOrPasswordFlow() {
        try {
            // Önce Subscribe var mı?
            if (utils.waitUntilVisible(SUBSCRIBE_BTN, 3)) {
                LoggerHelper.log(LogLevel.INFO, "Subscribe bulundu, performSubscription başlatılıyor...");
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                utils.waitAndClick(SUBSCRIBE_BTN, 3);
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                ScreenshotHelper.captureAndAttach(driver, "Subscribe Clicked", LogLevel.INFO);

                // TestUtils içindeki kendi akışın:
                utils.performSubscription();
                return;
            }

            LoggerHelper.log(LogLevel.INFO, "Subscribe bulunamadı, şifre alanı kontrol ediliyor...");

            // Şifre alanı
            if (utils.waitUntilVisible(PASSWORD_FIELD, 5)) {
                LoggerHelper.log(LogLevel.INFO, "Password alanı bulundu, şifre giriliyor...");

                try {
                    for (int i = 0; i < 3; i++) {  // 3 kez dene, ekran değişirse tekrar bul
                        if (!utils.waitUntilVisible(PASSWORD_FIELD, 3)) continue;

                        WebElement pwdField = driver.findElement(PASSWORD_FIELD);

                        try {
                            pwdField.click();
                            Thread.sleep(500); // keyboard odak bekleme

                            pwdField.clear();
                            Thread.sleep(500); // clear sonrası UI refresh olabiliyor

                            pwdField.sendKeys("S.tudio1234");
                            LoggerHelper.log(LogLevel.INFO, "Şifre başarıyla girildi.");
                            break; // başarılı oldu
                        } catch (Exception inner) {
                            LoggerHelper.log(LogLevel.WARN, "Element kayboldu, tekrar denenecek...");
                            Thread.sleep(1000);  // UI stabilize olana kadar bekle
                        }
                    }
                } catch (Exception e) {
                    try {
                        LoggerHelper.log(LogLevel.WARN, "Fallback: utils.inputValue ile şifre giriliyor...");
                        utils.inputValue(PASSWORD_FIELD, "S.tudio1234", 5);
                    } catch (Exception ex) {
                        LoggerHelper.log(LogLevel.ERROR, "Şifre alanına yazılamadı (fallback): " + ex.getMessage());
                    }
                }


                // Sign In
                if (utils.waitUntilVisible(SIGN_IN_BTN, 5)) {
                    utils.waitAndClick(SIGN_IN_BTN, 5);
                    LoggerHelper.log(LogLevel.INFO, "Sign In tıklandı, bekleniyor...");
                    try { Thread.sleep(30000); } catch (InterruptedException ignored) {}
                } else {
                    LoggerHelper.log(LogLevel.WARN, "Sign In butonu bulunamadı.");
                }
            } else {
                LoggerHelper.log(LogLevel.WARN, "Ne Subscribe ne de password alanı bulunabildi.");
            }
        } catch (Exception e) {
            LoggerHelper.log(LogLevel.ERROR, "Akış sırasında hata: " + e.getMessage());
        }
    }

    // ===================== High-level Flow =====================

    /**
     * Onboarding/Welcome akışının tamamını yürütür:
     * 1) NEXT'lere max 4 tık
     * 2) Intro swipe'ları
     * 3) Öncelikli butonlardan ilk görüneni tıkla (2 tur dene)
     * 4) Subscribe varsa -> subscription, yoksa password Sign-In
     */
    public void completeOnboardingFlow() {
        LoggerHelper.log(LogLevel.INFO, "Onboarding akışı başlatılıyor...");
        ScreenshotHelper.captureAndAttach(driver, "Onboarding-Start", LogLevel.INFO);

        tapNextUpTo(4);
        doIntroSwipes();

        int clickedCount = clickUpToThreePrimaryButtons();
        if (clickedCount > 0) {
            LoggerHelper.log(LogLevel.INFO, "Butonlar üzerinden akış ilerletildi, abonelik/şifre akışına geçiliyor...");
            subscribeOrPasswordFlow();
        } else {
            LoggerHelper.log(LogLevel.WARN, "Hiçbir buton tıklanamadı, akış sonlandırıldı.");
        }

        ScreenshotHelper.captureAndAttach(driver, "Onboarding-End", LogLevel.INFO);
        LoggerHelper.log(LogLevel.INFO, "Onboarding akışı tamamlandı.");
    }

    // ===================== Opsiyonel yardımcılar =====================

    public boolean isOpenButtonVisible() {
        return utils.waitUntilVisible(OPEN_BUTTON, 3);
    }

    public void tapOpenIfVisible() {
        if (isOpenButtonVisible()) {
            utils.waitAndClick(OPEN_BUTTON, 3);
            LoggerHelper.log(LogLevel.INFO, "'Open' tıklandı.");
        }
    }

    public boolean isAppIconVisible() {
        return utils.waitUntilVisible(APP_ICON, 3);
    }
}
