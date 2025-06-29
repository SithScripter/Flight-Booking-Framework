package com.demo.flightbooking.utils;

import com.aventstack.extentreports.ExtentTest;

/**
 * Manages thread-safe access to ExtentTest instances for parallel test execution.
 */
public class ExtentManager {

	private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    // Retrieve the ExtentTest for the current thread
    public static ExtentTest getTest() {
        return extentTest.get();
    }

    // Set the ExtentTest for the current thread
    public static void setTest(ExtentTest test) {
        extentTest.set(test);
    }

    // Remove the ExtentTest to avoid memory leaks
    public static void unload() {
        extentTest.remove();
    }
}