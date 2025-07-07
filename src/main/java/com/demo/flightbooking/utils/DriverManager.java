package com.demo.flightbooking.utils;

import com.demo.flightbooking.enums.BrowserType;
import com.demo.flightbooking.factory.BrowserOptionsFactory;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class DriverManager {

    private static final Logger logger = LogManager.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static final ThreadLocal<String> browserName = new ThreadLocal<>();

    public static void setBrowser(String browser) {
        logger.info("Setting browser for current thread to: {}", browser.toUpperCase());
        browserName.set(browser.toLowerCase());
    }

    public static String getBrowser() {
        return browserName.get();
    }

    public static WebDriver getDriver() {
        if (driver.get() == null) {
            String browser = browserName.get() != null
                    ? browserName.get()
                    : ConfigReader.getProperty("browser");

            // ✅ Load browser type from config or testng.xml
            BrowserType browserType = BrowserType.valueOf(browser.toUpperCase());

            // ✅ Read headless flag from config.properties
            boolean isHeadless = Boolean.parseBoolean(ConfigReader.getProperty("browser.headless"));
            logger.info("Headless mode enabled? {}", isHeadless);

            // ✅ Read Grid toggle
            boolean useGrid = Boolean.parseBoolean(ConfigReader.getProperty("selenium.grid.enabled"));
            logger.info("Grid enabled? {}", useGrid);
            logger.info("Execution mode: {}", useGrid ? "REMOTE (Grid)" : "LOCAL");
            logger.info("Initializing {} driver for thread: {}", browserType, Thread.currentThread().threadId());

            // ✅ Fetch browser-specific options with headless flag
            MutableCapabilities options = BrowserOptionsFactory.getOptions(browserType, isHeadless);

            if (useGrid) {
                try {
                    // Validate required properties
                    String hubHost = ConfigReader.getProperty("selenium.hubHost");
                    String urlFormat = ConfigReader.getProperty("seleniumhub.urlFormat");

                    if (hubHost == null || hubHost.isEmpty()) {
                        throw new RuntimeException("Missing hubHost or urlFormat in config.properties");
                    }
                    if (urlFormat == null || urlFormat.isEmpty()) {
                        throw new RuntimeException("⚠️ seleniumhub.urlFormat property is missing in config.properties");
                    }

                    String fullUrl = String.format(urlFormat, hubHost);
                    logger.info("Connecting to Selenium Grid at: {}", fullUrl);

                    URL gridUrl = URI.create(fullUrl).toURL(); // Safe in Java 20+

                    driver.set(new RemoteWebDriver(gridUrl, options));
                } catch (MalformedURLException e) {
                    logger.error("❌ Malformed Selenium Grid URL: {}", e.getMessage());
                    throw new RuntimeException("Invalid Selenium Grid URL", e);
                }
            } else {
                // Local Mode
                switch (browserType) {
                    case CHROME:
                        driver.set(new ChromeDriver((ChromeOptions) options));
                        break;
                    case FIREFOX:
                        driver.set(new FirefoxDriver((FirefoxOptions) options));
                        break;
                    case EDGE:
                        driver.set(new EdgeDriver((EdgeOptions) options));
                        break;
                    default:
                        throw new IllegalStateException("Unsupported browser type: " + browserType);
                }
            }

            driver.get().manage().window().maximize();
            driver.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        }

        return driver.get();
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            logger.info("Quitting driver for thread: {}", Thread.currentThread().threadId());
            driver.get().quit();
            driver.remove();
            browserName.remove();
        }
    }
}
