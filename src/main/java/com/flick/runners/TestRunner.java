package com.flick.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = "src/test/java/features",
        glue = {"stepDefinitions"},  // SuiteHooks da bu pakette
        plugin = {
                "pretty",
                "html:target/cucumber-report.html",
                "json:target/cucumber-report.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"  // Allure raporu
        },
        monochrome = true
)
public class TestRunner extends AbstractTestNGCucumberTests {
}
