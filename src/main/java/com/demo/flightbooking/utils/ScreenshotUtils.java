package com.demo.flightbooking.utils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotUtils {

    private static final String SCREENSHOT_DIR = "reports/screenshots/";

    public static String captureScreenshot(WebDriver driver, String testName) {
        // Create folder if it doesn’t exist
        File dir = new File(SCREENSHOT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = testName + "_" + timestamp + ".png";
        String relativePath = "screenshots/" + fileName;
        String fullPath = SCREENSHOT_DIR + fileName;

        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(srcFile, new File(fullPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Only return the relative path
        return relativePath.replace("\\", "/"); // important for Windows
    }
}
