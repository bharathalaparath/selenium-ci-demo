package utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class ScreenshotUtils {

    // Set by Hooks.tearDown() (while the driver is still alive) and read by
    // ExtentTestNGListener (which fires after the driver has already quit).
    public static final ThreadLocal<String> LAST_FAILURE_SCREENSHOT = new ThreadLocal<>();

    public static String captureBase64(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
    }
}
