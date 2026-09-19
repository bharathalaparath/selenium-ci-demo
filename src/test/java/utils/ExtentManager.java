package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExtentManager {

    // Keyed by report name (Smoke/Regression) rather than a single static
    // instance, so both stay separate even if a suite ever runs them in one
    // JVM (e.g. plain `mvn test` with no -Dtest filter).
    private static final Map<String, ExtentReports> INSTANCES = new ConcurrentHashMap<>();

    public static ExtentReports getInstance(String reportName) {
        return INSTANCES.computeIfAbsent(reportName, name -> {
            ExtentSparkReporter spark = new ExtentSparkReporter("target/extent-reports/" + name + ".html");
            ExtentReports extent = new ExtentReports();
            extent.attachReporter(spark);
            return extent;
        });
    }
}
