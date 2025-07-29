package com.flick.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

import java.util.Arrays;

@CucumberOptions(
        features = {
                "src/test/java/features/testFlight.feature",
                "src/test/java/features/onboard.feature"
        },
        glue = {"stepDefinitions"},
        plugin = {
                "pretty",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        publish = false
)
public class TestRunner extends AbstractTestNGCucumberTests {

        @Override
        @DataProvider(parallel = false) // Tek cihaz, tek thread
        public Object[][] scenarios() {
                Object[][] scenarios = super.scenarios();

                Arrays.sort(scenarios, (a, b) -> {
                        String featureA = ((io.cucumber.testng.FeatureWrapper) a[1])
                                .toString().toLowerCase();
                        String featureB = ((io.cucumber.testng.FeatureWrapper) b[1])
                                .toString().toLowerCase();

                        // Önce testFlight.feature, sonra onboard.feature
                        if (featureA.contains("testflight") && featureB.contains("onboard")) return -1;
                        if (featureA.contains("onboard") && featureB.contains("testflight")) return 1;
                        return featureA.compareTo(featureB);
                });

                return scenarios;
        }
}
