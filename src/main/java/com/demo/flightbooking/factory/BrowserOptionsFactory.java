package com.demo.flightbooking.factory;

import com.demo.flightbooking.enums.BrowserType;
import com.demo.flightbooking.utils.ConfigReader;

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

    public static MutableCapabilities getOptions(BrowserType browserType, boolean isHeadless) {
        logger.info("Creating options for browser: {}", browserType);
        
//        // ✅ Load headless config from config.properties (default: false)
//        isHeadless = Boolean.parseBoolean(ConfigReader.getProperty("browser.headless"));
        logger.info("Headless mode for {}: {}", browserType, isHeadless);
        
        switch (browserType) {
            case CHROME:
                // --- THIS IS THE CHANGE ---
                // WebDriverManager setup is now handled here.
                WebDriverManager.chromedriver().setup();	
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--start-maximized");
                chromeOptions.addArguments("--disable-gpu");
                chromeOptions.addArguments("--remote-allow-origins=*");
                
                // ✅ Add headless options if enabled
                if (isHeadless) {
                    logger.info("✅ Enabling headless mode for CHROME");
                    chromeOptions.addArguments("--headless=new"); // Use `--headless=new` for Chrome 109+
                    chromeOptions.addArguments("--window-size=1920,1080");
                }
                
                return chromeOptions;

            case FIREFOX:
                // --- THIS IS THE CHANGE ---
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                
                // ✅ Add headless for Firefox
                if (isHeadless) {
                    logger.info("✅ Enabling headless mode for FIREFOX");
                    firefoxOptions.addArguments("--headless");
                    firefoxOptions.addArguments("--width=1920");
                    firefoxOptions.addArguments("--height=1080");
                }
                return firefoxOptions;

            case EDGE:
                // --- THIS IS THE CHANGE ---
                WebDriverManager.edgedriver().setup();
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments("--start-maximized");
                edgeOptions.addArguments("--inprivate");
                
                if (isHeadless) {
                    logger.info("✅ Enabling headless mode for EDGE");
                    edgeOptions.addArguments("--headless=new");
                    edgeOptions.addArguments("--window-size=1920,1080");
                }
                
                return edgeOptions;

            default:
                throw new IllegalArgumentException("Unsupported browser type provided: " + browserType);
        }
    }
}