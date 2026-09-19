package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import io.cucumber.testng.PickleWrapper;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import utils.ExtentManager;
import utils.ScreenshotUtils;

public class ExtentTestNGListener implements ITestListener {

    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();
    private ExtentReports extent;

    @Override
    public void onStart(ITestContext context) {
        // NOT getAllTestMethods()[0].getRealClass() -- runScenario() is declared on
        // AbstractTestNGCucumberTests, so that returns the abstract base class for
        // every runner. getInstance().getClass() gives the actual runtime subclass
        // (SmokeTestRunner / RegressionTestRunner).
        String runnerName = context.getAllTestMethods()[0].getInstance().getClass().getSimpleName();
        extent = ExtentManager.getInstance(runnerName);
    }

    @Override
    public void onTestStart(ITestResult result) {
        CURRENT_TEST.set(extent.createTest(scenarioName(result)));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        CURRENT_TEST.get().log(Status.PASS, "Scenario passed");
        CURRENT_TEST.remove();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        recordFailure(result);
    }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
        recordFailure(result);
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }

    private void recordFailure(ITestResult result) {
        ExtentTest test = CURRENT_TEST.get();
        // Read, not captured here -- the driver is already quit by the time this
        // listener fires. Hooks.tearDown() captured it while the session was alive.
        String screenshot = ScreenshotUtils.LAST_FAILURE_SCREENSHOT.get();
        try {
            if (screenshot != null) {
                test.fail(result.getThrowable(),
                        MediaEntityBuilder.createScreenCaptureFromBase64String(screenshot).build());
            } else {
                test.fail(result.getThrowable());
            }
        } finally {
            ScreenshotUtils.LAST_FAILURE_SCREENSHOT.remove();
            CURRENT_TEST.remove();
        }
    }

    private String scenarioName(ITestResult result) {
        // AbstractTestNGCucumberTests has exactly one @Test method (runScenario) for
        // every scenario, so result.getName() is always "runScenario" -- the real
        // name lives in the Cucumber PickleWrapper parameter TestNG passed it.
        Object[] params = result.getParameters();
        if (params.length > 0 && params[0] instanceof PickleWrapper) {
            return ((PickleWrapper) params[0]).getPickle().getName();
        }
        return result.getName();
    }
}
