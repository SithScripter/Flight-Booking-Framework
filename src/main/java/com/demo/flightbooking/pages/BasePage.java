package com.demo.flightbooking.pages;

import com.demo.flightbooking.utils.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Base class for all Page Objects.
 * Centralizes WebDriver and WebDriverWait initialization, and provides common utilities
 * for all page classes.
 */
public abstract class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected final Logger logger; // Logger for each page object

    private static final int DEFAULT_TIMEOUT = 10; // Default timeout if not specified in config

    public BasePage(WebDriver driver) {
        this.driver = driver;
        // Initialize logger with the specific class name of the concrete page object
        this.logger = LogManager.getLogger(this.getClass());
        // Initialize WebDriverWait using the timeout from config.properties
        // If "test.timeout" is not found or invalid, it will default to 10 seconds.
        int timeoutSeconds = ConfigReader.getPropertyAsInt("test.timeout");
        if (timeoutSeconds <= 0) {
            timeoutSeconds = DEFAULT_TIMEOUT; // Use default if config value is invalid or not found
            logger.warn("Invalid or missing 'test.timeout' in config.properties. Using default timeout: {} seconds.", DEFAULT_TIMEOUT);
        }
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));

        // Initialize PageFactory elements (if using @FindBy annotations)
        // PageFactory.initElements(driver, this); // Uncomment if you plan to use @FindBy
    }

    /**
     * Get the title of the current page.
     * @return The page title.
     */
    public String getPageTitle() {
        return driver.getTitle();
    }

    /**
     * Get the current URL.
     * @return The current URL.
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    // You can add more common methods here, e.g.,
    // public void navigateTo(String url) { driver.get(url); }
    // public boolean isElementPresent(By locator) { ... }
}