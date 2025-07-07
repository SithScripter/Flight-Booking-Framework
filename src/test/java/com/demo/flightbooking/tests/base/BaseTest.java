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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;

public class BaseTest {

  protected static final Logger logger = LogManager.getLogger(BaseTest.class);
  private static ExtentReports extentReports;
  protected static final List<String> failureSummaries =
      Collections.synchronizedList(new ArrayList<>());

  @BeforeSuite(alwaysRun = true)
  public void setUpSuite() {
    File logsDir = new File("logs");
    if (!logsDir.exists()) {
      logsDir.mkdirs();
    }
    logger.info("Logs directory ensured.");

    extentReports = new ExtentReports();
    
    // Read the 'test.suite' property passed from the Jenkinsfile/Maven command.
    String suiteName = System.getProperty("test.suite", "default");

    // This reporter creates a single self-contained offline file with a dynamic name.
    ExtentSparkReporter sparkReporter =
        new ExtentSparkReporter("reports/" + suiteName + "-report.html");
    sparkReporter.config().setOfflineMode(true);
    sparkReporter.config().setDocumentTitle("Test Report: " + suiteName.toUpperCase());
    
    extentReports.attachReporter(sparkReporter);
    logger.info("Report will be generated at: reports/{}-report.html", suiteName);

    extentReports.setSystemInfo("Tester", ConfigReader.getProperty("tester.name"));
    extentReports.setSystemInfo("OS", System.getProperty("os.name"));
    extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));
    extentReports.setSystemInfo("Browser", ConfigReader.getProperty("browser"));
  }
  
//  @Parameters("browser")
//  @BeforeClass(alwaysRun = true)
//  public void setBrowser(String browser) {
//      DriverManager.setBrowser(browser);
//      logger.info("✅ Browser set to: {} for test class: {}", browser.toUpperCase(), this.getClass().getSimpleName());
//  }
  

  /**
   * This method now runs before each @Test method and receives the browser parameter.
   * This ensures the browser is set correctly for the specific thread running the test.
   *
   * @param browser The browser name passed from the <parameter> tag in testng.xml.
   * @param method  The test method that is about to be run.
   */
  @Parameters("browser")
  @BeforeMethod(alwaysRun = true)
  public void setUp(String browser, Method method) {
    // --- THIS IS THE FIX ---
    // The very first step is to set the browser for the current thread.
    DriverManager.setBrowser(browser);

    // Now, when getDriver() is called, it will use the browser name set for its specific thread.
    DriverManager.getDriver();
    logger.info("WebDriver initialized for test: {}", method.getName());

    // Get the browser name again for reporting purposes.
    String browserName = DriverManager.getBrowser().toUpperCase();
    
    // Append the browser name to the test name in the report for clarity.
    ExtentTest test = extentReports.createTest(method.getName() + " - " + browserName);
    ExtentManager.setTest(test);
    logger.info("ExtentTest created for test: {} on {}", method.getName(), browserName);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown(ITestResult result) {
    ExtentTest test = ExtentManager.getTest();
    WebDriver driver = DriverManager.getDriver();

    if (test != null) {
      if (result.getStatus() == ITestResult.FAILURE) {
        String failureMsg =
            "❌ "
                + result.getMethod().getMethodName()
                + " FAILED: "
                + result.getThrowable().getMessage().split("\n")[0];
        failureSummaries.add(failureMsg);

        String screenshotPath =
            ScreenshotUtils.captureScreenshot(driver, result.getMethod().getMethodName());
        test.addScreenCaptureFromPath("./screenshots/" + new File(screenshotPath).getName());
        test.fail(result.getThrowable());
        logger.error(
            "Test failed: {} | Screenshot: {}", result.getMethod().getMethodName(), screenshotPath);
      } else {
        test.log(Status.PASS, "Test passed");
      }
    }

    DriverManager.quitDriver();
    logger.info("WebDriver quit after test method: {}", result.getMethod().getMethodName());
    ExtentManager.unload();
  }

  @AfterSuite(alwaysRun = true)
  public void tearDownSuite() {
    if (extentReports != null) {
      extentReports.flush();
      logger.info("✅ ExtentReports flushed to file.");
    }

    String suiteName = System.getProperty("test.suite", "default");
    String reportFileName = suiteName + "-report.html";
    String summaryFileName = suiteName + "-failure-summary.txt";

    if (!failureSummaries.isEmpty()) {
      try (PrintWriter out = new PrintWriter("reports/" + summaryFileName)) {
        out.println("===== FAILED TEST SUMMARY =====");
        failureSummaries.forEach(out::println);
        logger.info("✅ Failure summary written: {}", summaryFileName);
      } catch (IOException e) {
        logger.error("❌ Failed to write failure summary", e);
      }
    }

    // Copy the dynamic report to index.html for the Jenkins publisher
    try {
      Path source = Paths.get("reports", reportFileName);
      Path target = Paths.get("reports", "index.html");
      if (Files.exists(source)) {
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        logger.info("✅ Report copied to index.html for Jenkins display.");
      }
    } catch (IOException e) {
      logger.error("❌ Failed to copy report to index.html", e);
    }
  }
}