package com.demo.flightbooking.tests.base;

import java.io.File;
import java.lang.reflect.Method;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite; // Changed from @AfterClass
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite; // Changed from @BeforeClass

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.demo.flightbooking.utils.ConfigReader;
import com.demo.flightbooking.utils.DriverManager;
import com.demo.flightbooking.utils.ExtentManager;
import com.demo.flightbooking.utils.ScreenshotUtils;

public class BaseTest {

    protected static final Logger logger = LogManager.getLogger(BaseTest.class);
    // The single ExtentReports instance for the entire test suite
    private static ExtentReports extentReports;

    /**
     * This method runs once before the entire test suite starts.
     * It initializes the ExtentReports object and sets up system information.
     */
    @BeforeSuite(alwaysRun = true)
    public void setUpSuite() {
        File logsDir = new File("logs");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }
        logger.info("Logs directory ensured.");

        // Initialize ExtentReports
        String reportPath = "reports/extent-report.html";
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        sparkReporter.config().setDocumentTitle("Flight Booking Automation Report");
        sparkReporter.config().setReportName("Flight Booking Test Results");
        sparkReporter.config().setTimeStampFormat("MMM dd, yyyy HH:mm:ss");

        extentReports = new ExtentReports();
        extentReports.attachReporter(sparkReporter);

        // Set system information for the report
        extentReports.setSystemInfo("Tester", ConfigReader.getProperty("tester.name"));
        extentReports.setSystemInfo("OS", System.getProperty("os.name"));
        extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));
        extentReports.setSystemInfo("Browser", ConfigReader.getProperty("browser"));
        logger.info("ExtentReports initialized for the suite.");
    }

    /**
     * This method runs before each @Test method.
     * It initializes the WebDriver and creates a new test entry in the report.
     */
    @BeforeMethod(alwaysRun = true)
    public void setUp(Method method) {
        DriverManager.getDriver();
        logger.info("WebDriver initialized for test: {}", method.getName());

        ExtentTest test = extentReports.createTest(method.getName());
        ExtentManager.setTest(test);
        logger.info("ExtentTest created for test: {}", method.getName());
    }

    /**
     * This method runs after each @Test method.
     * It logs the test status, captures a screenshot on failure, and quits the driver.
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        WebDriver driver = DriverManager.getDriver();

        if (test != null) {
            if (result.getStatus() == ITestResult.FAILURE) {
                String screenshotPath = ScreenshotUtils.captureScreenshot(driver, result.getMethod().getMethodName());
                String relativePathForReport = "./screenshots/" + new File(screenshotPath).getName();
                test.fail("Test failed: " + result.getThrowable());
                test.addScreenCaptureFromPath(relativePathForReport, "Failure Screenshot");
                logger.error("Test failed: {} | Screenshot: {}", result.getMethod().getMethodName(), screenshotPath);
            } else if (result.getStatus() == ITestResult.SKIP) {
                // The TestListener will handle logging for retried tests
            } else {
                test.log(Status.PASS, "Test passed");
            }
        }

        DriverManager.quitDriver();
        logger.info("WebDriver quit after test method: {}", result.getMethod().getMethodName());
        ExtentManager.unload();
    }

    /**
     * This method runs once after the entire test suite has finished.
     * It writes all the test information from memory to the HTML report file.
     */
    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        if (extentReports != null) {
            extentReports.flush();
            logger.info("ExtentReports flushed to file.");
        }
    }
}