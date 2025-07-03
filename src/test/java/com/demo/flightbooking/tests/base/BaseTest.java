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

public class BaseTest {

  protected static final Logger logger = LogManager.getLogger(BaseTest.class);
  // The single ExtentReports instance for the entire test suite
  private static ExtentReports extentReports;
  
  // --- 1. DECLARATION: A thread-safe list to collect failure messages ---
  protected static final List<String> failureSummaries =
      Collections.synchronizedList(new ArrayList<>());

  /**
   * This method runs once before the entire test suite starts. It initializes the ExtentReports
   * object and sets up system information.
   */
  @BeforeSuite(alwaysRun = true)
  public void setUpSuite() {
      File logsDir = new File("logs");
      if (!logsDir.exists()) {
          logsDir.mkdirs();
      }
      logger.info("Logs directory ensured.");
      
      // Read the 'test.suite' property passed from the POM, with a fallback name of 'default'.
      String suiteName = System.getProperty("test.suite", "default");

      extentReports = new ExtentReports();

      // This reporter creates a single self-contained file with a dynamic name for email attachments.
      ExtentSparkReporter sparkReporter = new ExtentSparkReporter("reports/" + suiteName + "-report.html");
      sparkReporter.config().setOfflineMode(true);
      sparkReporter.config().setDocumentTitle("Test Report: " + suiteName.toUpperCase());
      sparkReporter.config().setReportName("Test Execution Report");
      
      extentReports.attachReporter(sparkReporter);

      // Set system information...
      extentReports.setSystemInfo("Tester", ConfigReader.getProperty("tester.name"));
      extentReports.setSystemInfo("OS", System.getProperty("os.name"));
      extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));
      extentReports.setSystemInfo("Browser", ConfigReader.getProperty("browser"));
  }


  /**
   * This method runs before each @Test method. It initializes the WebDriver and creates a new test
   * entry in the report.
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
   * This method runs after each @Test method. It logs the test status, captures a screenshot on
   * failure, and quits the driver.
   */
  @AfterMethod(alwaysRun = true)
  public void tearDown(ITestResult result) {
    ExtentTest test = ExtentManager.getTest();
    WebDriver driver = DriverManager.getDriver();

    if (test != null) {
      if (result.getStatus() == ITestResult.FAILURE) {
    	  
    	  // --- 2. POPULATION: If a test fails, we add its details to our list ---
          String failureMsg =
              "❌ "
                  + result.getMethod().getMethodName()
                  + " FAILED: "
                  + result.getThrowable().getMessage().split("\n")[0]; // Get first line of error
          failureSummaries.add(failureMsg);
    	  
        String screenshotPath =
            ScreenshotUtils.captureScreenshot(driver, result.getMethod().getMethodName());
        String relativePathForReport = "./screenshots/" + new File(screenshotPath).getName();
        test.fail("Test failed: " + result.getThrowable());
        test.addScreenCaptureFromPath(relativePathForReport, "Failure Screenshot");
        logger.error(
            "Test failed: {} | Screenshot: {}", result.getMethod().getMethodName(), screenshotPath);
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
   * This method runs once after the entire test suite has finished. It writes all the test
   * information from memory to the HTML report file.
   */
  @AfterSuite(alwaysRun = true)
  public void tearDownSuite() {
      if (extentReports != null) {
          extentReports.flush();
          logger.info("ExtentReports flushed to file.");
      }

      String suiteName = System.getProperty("test.suite", "default");

      // 1️ Write failure summary if any
      if (!failureSummaries.isEmpty()) {
          try (PrintWriter out = new PrintWriter("reports/" + suiteName + "-failure-summary.txt")) {
              out.println("===== FAILED TEST SUMMARY =====");
              for (String fail : failureSummaries) {
                  out.println(fail);
              }
              logger.info("Failure summary written to file.");
          } catch (IOException e) {
              logger.error("Failed to write failure summary.", e);
          }
      }

      // 2️ Copy the suite report to index.html so Jenkins publishHTML always works
      try {
          java.nio.file.Files.copy(
              java.nio.file.Paths.get("reports/" + suiteName + "-report-offline.html"),
              java.nio.file.Paths.get("reports/index.html"),
              java.nio.file.StandardCopyOption.REPLACE_EXISTING
          );
          logger.info("Report copied to reports/index.html for Jenkins UI compatibility.");
      } catch (IOException e) {
          logger.error("Failed to copy report to index.html", e);
      }
  }
}