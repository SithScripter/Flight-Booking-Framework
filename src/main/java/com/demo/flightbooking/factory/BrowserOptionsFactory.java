package com.demo.flightbooking.factory;

import com.demo.flightbooking.enums.BrowserType;
import io.github.bonigarcia.wdm.WebDriverManager; // Import WebDriverManager
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * Returns browser-specific options after setting up WebDriverManager.
 *
 * @param browserType Enum for supported browsers.
 * @return MutableCapabilities instance with browser-specific options.
 */

public class BrowserOptionsFactory {

    private static final Logger logger = LogManager.getLogger(BrowserOptionsFactory.class);

    public static MutableCapabilities getOptions(BrowserType browserType) {
        logger.info("Creating options for browser: {}", browserType);
        switch (browserType) {
            case CHROME:
                // --- THIS IS THE CHANGE ---
                // WebDriverManager setup is now handled here.
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--start-maximized");
                chromeOptions.addArguments("--disable-gpu");
                chromeOptions.addArguments("--remote-allow-origins=*");
                return chromeOptions;

            case FIREFOX:
                // --- THIS IS THE CHANGE ---
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                // Add any Firefox-specific options here if needed
                return firefoxOptions;

            case EDGE:
                // --- THIS IS THE CHANGE ---
                WebDriverManager.edgedriver().setup();
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments("--start-maximized");
                edgeOptions.addArguments("--inprivate");
                return edgeOptions;

            default:
                throw new IllegalArgumentException("Unsupported browser type provided: " + browserType);
        }
    }
}