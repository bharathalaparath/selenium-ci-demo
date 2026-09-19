package stepdefinitions;

import factory.DriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.WebDriver;
import utils.ScreenshotUtils;

import java.util.Base64;

public class Hooks {

    @Before
    public void setUp() {
        DriverFactory.initDriver();
    }

    @After
    public void tearDown(Scenario scenario) {
        WebDriver driver = DriverFactory.getDriver();
        if (scenario.isFailed() && driver != null) {
            // Must happen before quitDriver() below -- once the session closes,
            // there's nothing left to screenshot.
            String base64Screenshot = ScreenshotUtils.captureBase64(driver);
            scenario.attach(Base64.getDecoder().decode(base64Screenshot), "image/png", "Failure Screenshot");
            ScreenshotUtils.LAST_FAILURE_SCREENSHOT.set(base64Screenshot);
        }
        DriverFactory.quitDriver();
    }
}
