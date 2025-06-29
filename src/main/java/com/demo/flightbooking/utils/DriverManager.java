package com.demo.flightbooking.utils;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.edge.EdgeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * DriverManager is a utility class that handles creation, access, and cleanup of WebDriver instances.
 * <p>
 * It ensures thread-safe browser sessions using ThreadLocal, which is crucial for parallel test execution.
 */
public class DriverManager {

    // Logger to record driver-related actions (initialization, errors, cleanup)
    private static final Logger logger = LogManager.getLogger(DriverManager.class);

    /**
     * Thread-local storage to ensure each parallel test thread gets its own isolated WebDriver instance.
     * <p>
     * Prevents test interference during parallel execution.
     */
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    /**
     * Returns the WebDriver instance for the current thread.
     * <p>
     * If not already initialized, this method creates a new driver instance based on config/browser property.
     *
     * @return WebDriver instance for the calling thread
     */
    public static WebDriver getDriver() {
        if (driver.get() == null) {
            // Read browser value from system property first, then fallback to config
            String browser = System.getProperty("browser", ConfigReader.getProperty("browser")).toLowerCase();

            logger.info("Initializing {} driver", browser);

            // Setup and initialize driver based on browser type
            switch (browser) {
                case "chrome":
                    WebDriverManager.chromedriver().setup();
                    driver.set(new ChromeDriver());
                    break;
                case "firefox":
                    WebDriverManager.firefoxdriver().setup();
                    driver.set(new FirefoxDriver());
                    break;
                case "edge":
                    WebDriverManager.edgedriver().setup();
                    driver.set(new EdgeDriver());
                    break;
                default:
                    // If an unsupported browser is passed, throw runtime error
                    logger.error("Unsupported browser: {}", browser);
                    throw new RuntimeException("Unsupported browser: " + browser);
            }

            // Basic browser setup — maximize and disable implicit wait
            driver.get().manage().window().maximize();
            driver.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(0)); // Explicit waits preferred
        }

        return driver.get();
    }

    /**
     * Cleans up and quits the WebDriver instance for the current thread.
     * <p>
     * Called at the end of each test to avoid memory leaks or session issues.
     */
    public static void quitDriver() {
        if (driver.get() != null) {
            logger.info("Quitting driver");
            driver.get().quit();    // Closes browser window
            driver.remove();        // Clears ThreadLocal to prevent stale reference
        }
    }
}
