package stepDefinitions;

import com.flick.pages.OnboardingPage;
import com.flick.utils.LogLevel;
import com.flick.utils.LoggerHelper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;

public class OnboardingSteps {

    private final OnboardingPage onboardingPage = new OnboardingPage();

    @And("Onboarding akışı tamamlanır")
    public void completeOnboarding() {
        onboardingPage.completeOnboardingFlow();
    }

    @Then("Open butonu görünürse tıklanır")
    public void tapOpenIfVisible() {
        onboardingPage.tapOpenIfVisible();
        LoggerHelper.log(LogLevel.INFO, "Open butonu kontrol edildi.");
    }
}
