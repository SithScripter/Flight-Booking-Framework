package com.demo.flightbooking.listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

// Import ConfigReader from the utils package
import com.demo.flightbooking.utils.ConfigReader;

/**
 * A TestNG retry analyzer that retries a failed test a configurable number of times.
 * This class resides in the 'listeners' package.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private int retryCount = 0;
    
    // Read the max retry count from the config file.
    private static final int maxRetryCount = ConfigReader.getPropertyAsInt("test.retry.maxcount");

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < maxRetryCount) {
            retryCount++;
            return true; // Return true to signal TestNG to retry the test
        }
        return false; // Return false to stop retrying
    }
}