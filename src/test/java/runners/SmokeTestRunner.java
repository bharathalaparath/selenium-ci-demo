package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = "stepdefinitions",
        tags = "@smoke",                          // Only smoke scenarios
        plugin = {
                "pretty",
                "html:target/cucumber-reports/smoke-report.html",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        }
)
public class SmokeTestRunner extends AbstractTestNGCucumberTests {
}