package com.demo.flightbooking.tests.base;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.demo.flightbooking.utils.ConfigReader;
import com.demo.flightbooking.utils.DriverManager;
import com.demo.flightbooking.utils.ExtentManager;
import com.demo.flightbooking.utils.ScreenshotUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

/**
 * The base class for all test classes in the framework.
 * It handles the core setup and teardown logic for test execution, reporting, and logging.
 */
public class BaseTest {

  protected static final Logger logger = LogManager.getLogger(BaseTest.class);
  private static ExtentReports extentReports;

  /**
   * A thread-safe list to collect failure messages from parallel tests.
   * 'static' ensures there's only one list per test run.
   * 'synchronizedList' prevents race conditions when tests run in parallel.
   */
  protected static final List<String> failureSummaries =
      Collections.synchronizedList(new ArrayList<>());

  /**
   * This method runs once before the entire test suite starts.
   * It is responsible for setting up the ExtentReports instance.
   */
  @BeforeSuite(alwaysRun = true)
  public void setUpSuite() {
    File logsDir = new File("logs");
    if (!logsDir.exists()) {
      logsDir.mkdirs();
    }
    logger.info("Logs directory ensured.");

    // Read the 'test.suite' property passed from the pom.xml, with a fallback name.
    String suiteName = System.getProperty("test.suite", "default");

    // Initialize the main ExtentReports object.
    extentReports = new ExtentReports();

    // Configure the reporter to create a single, self-contained offline HTML file.
    // The filename is dynamic based on the Maven profile being run (e.g., "smoke-report.html").
    ExtentSparkReporter sparkReporter =
        new ExtentSparkReporter("reports/" + suiteName + "-report.html");
    sparkReporter.config().setOfflineMode(true);
    sparkReporter.config().setDocumentTitle("Test Report: " + suiteName.toUpperCase());
    sparkReporter.config().setReportName("Test Execution Report");

    // Attach the configured reporter to our main reports object.
    extentReports.attachReporter(sparkReporter);
    logger.info("Generating report at: reports/{}-report.html", suiteName);

    // Set system information that will be displayed in the report dashboard.
    extentReports.setSystemInfo("Tester", ConfigReader.getProperty("tester.name"));
    extentReports.setSystemInfo("OS", System.getProperty("os.name"));
    extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));
    extentReports.setSystemInfo("Browser", ConfigReader.getProperty("browser"));
  }

  /**
   * This method runs before each @Test method.
   * It initializes the WebDriver for the current thread and creates a new test entry in the report.
   *
   * @param method The test method that is about to be run.
   */
  @BeforeMethod(alwaysRun = true)
  public void setUp(Method method) {
    // Get a unique WebDriver instance for the current thread.
    DriverManager.getDriver();
    logger.info("WebDriver initialized for test: {}", method.getName());

    // Create a new test node in the Extent Report.
    ExtentTest test = extentReports.createTest(method.getName());
    // Store this test object in a ThreadLocal variable so it's accessible throughout the test.
    ExtentManager.setTest(test);
    logger.info("ExtentTest created for test: {}", method.getName());
  }

  /**
   * This method runs after each @Test method completes.
   * It logs the test status, captures a screenshot on failure, and quits the driver.
   *
   * @param result The result of the test method that has just run.
   */
  @AfterMethod(alwaysRun = true)
  public void tearDown(ITestResult result) {
    ExtentTest test = ExtentManager.getTest();
    WebDriver driver = DriverManager.getDriver();

    if (test != null) {
      // Logic for handling a failed test.
      if (result.getStatus() == ITestResult.FAILURE) {
        // Create a concise failure message and add it to our list for the email summary.
        String failureMsg =
            "❌ "
                + result.getMethod().getMethodName()
                + " FAILED: "
                + result.getThrowable().getMessage().split("\n")[0]; // Get first line of error message
        failureSummaries.add(failureMsg);

        // Capture a screenshot and attach it to the Extent Report.
        String screenshotPath =
            ScreenshotUtils.captureScreenshot(driver, result.getMethod().getMethodName());
        String relativePathForReport = "./screenshots/" + new File(screenshotPath).getName();
        test.fail("Test failed: " + result.getThrowable());
        test.addScreenCaptureFromPath(relativePathForReport, "Failure Screenshot");
        logger.error(
            "Test failed: {} | Screenshot: {}", result.getMethod().getMethodName(), screenshotPath);
      } else if (result.getStatus() == ITestResult.SKIP) {
        // Log skipped tests.
        test.log(Status.SKIP, "Test skipped");
      } else {
        // Log passed tests.
        test.log(Status.PASS, "Test passed");
      }
    }

    // Quit the WebDriver instance for the current thread.
    DriverManager.quitDriver();
    logger.info("WebDriver quit after test method: {}", result.getMethod().getMethodName());
    // Clean up the ExtentTest object for the current thread to prevent memory leaks.
    ExtentManager.unload();
  }

  /**
   * This method runs once after the entire test suite has finished.
   * It writes the Extent Report to the HTML file and creates the failure summary file.
   */
  @AfterSuite(alwaysRun = true)
  public void tearDownSuite() {
      // First, ensure the report is written to its original file (e.g., smoke-report.html)
      if (extentReports != null) {
          extentReports.flush();
          logger.info("✅ ExtentReports flushed to file.");
      }

      String suiteName = System.getProperty("test.suite", "default");
      String reportFileName = suiteName + "-report.html";
      String summaryFileName = suiteName + "-failure-summary.txt";

      // Write the failure summary file, as before.
      if (!failureSummaries.isEmpty()) {
          try (PrintWriter out = new PrintWriter("reports/" + summaryFileName)) {
              out.println("===== FAILED TEST SUMMARY =====");
              for (String fail : failureSummaries) {
                  out.println(fail);
              }
              logger.info("✅ Failure summary written: " + summaryFileName);
          } catch (IOException e) {
              logger.error("❌ Failed to write failure summary", e);
          }
      }

      // --- THIS IS THE FIX ---
      // Copy the dynamically named report to a generic 'index.html' for the Jenkins publisher.
      try {
          Path source = Paths.get("reports", reportFileName);
          Path target = Paths.get("reports", "index.html");

          if (Files.exists(source)) {
              Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
              logger.info("✅ Report successfully copied to index.html for Jenkins display.");
          } else {
              logger.error("❌ Source report file for copying not found: " + source.toString());
          }
      } catch (IOException e) {
          logger.error("❌ Failed to copy report to index.html", e);
      }
  }
}