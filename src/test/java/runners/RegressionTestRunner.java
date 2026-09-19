package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import listeners.ExtentTestNGListener;
import org.testng.annotations.Listeners;

@Listeners(ExtentTestNGListener.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "stepdefinitions",
        tags = "@regression or @edge-case",       // Regression + Edge cases
        plugin = {
                "pretty",
                "html:target/cucumber-reports/regression-report.html",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        }
)
public class RegressionTestRunner extends AbstractTestNGCucumberTests {
}