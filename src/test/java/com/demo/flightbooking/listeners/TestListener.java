package com.demo.flightbooking.listeners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.IAnnotationTransformer;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.demo.flightbooking.utils.ExtentManager;

/**
 * A comprehensive TestNG listener that handles applying the RetryAnalyzer.
 */
public class TestListener implements ITestListener, IAnnotationTransformer {

	@Override
	public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
		annotation.setRetryAnalyzer(RetryAnalyzer.class);
	}

	// ... other onTestStart, onTestSuccess methods remain the same

	@Override
	public void onTestStart(ITestResult result) {
		ExtentTest test = ExtentManager.getTest();
		if (test != null) {
			test.log(Status.INFO, "Test Started: " + result.getMethod().getMethodName());
		}
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		ExtentTest test = ExtentManager.getTest();
		if (test != null) {
			test.log(Status.PASS, "Test Passed: " + result.getMethod().getMethodName());
		}
	}

	@Override
	public void onTestFailure(ITestResult result) {
		ExtentTest test = ExtentManager.getTest();
		if (test != null) {
			test.log(Status.FAIL, "Test Failed: " + result.getMethod().getMethodName());
			test.log(Status.FAIL, result.getThrowable());
		}
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		ExtentTest test = ExtentManager.getTest();
		if (test != null) {
			if (result.wasRetried()) {
				test.log(Status.WARNING, "Test Retried: " + result.getMethod().getMethodName());
			} else {
				test.log(Status.SKIP, "Test Skipped: " + result.getMethod().getMethodName());
			}
		}
	}
}