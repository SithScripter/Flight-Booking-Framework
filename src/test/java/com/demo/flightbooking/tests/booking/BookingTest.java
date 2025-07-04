package com.demo.flightbooking.tests.booking;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentTest;
import com.demo.flightbooking.model.Passenger;
import com.demo.flightbooking.pages.FlightSelectionPage;
import com.demo.flightbooking.pages.HomePage;
import com.demo.flightbooking.pages.PurchasePage;
import com.demo.flightbooking.tests.base.BaseTest;
import com.demo.flightbooking.utils.ConfigReader;
import com.demo.flightbooking.utils.CsvDataProvider;
import com.demo.flightbooking.utils.DriverManager;
import com.demo.flightbooking.utils.ExtentManager;
import com.demo.flightbooking.utils.JsonDataProvider;
import com.demo.flightbooking.utils.WebDriverUtils;

/**
 * Test class for booking flights, utilizing data providers for all test data.
 * Extends BaseTest for WebDriver and reporting setup/teardown.
 */
public class BookingTest extends BaseTest {

    /**
     * Test method to perform flight booking using passenger data from JSON.
     * This test is part of the "regression" and "smoke" groups, covering the main E2E flow.
     *
     * @param passenger Passenger record containing personal, payment, and flight details from JSON.
     */
    @Test(
            dataProvider = "passengerData", 
            dataProviderClass = JsonDataProvider.class,
            groups = {"regression", "smoke", "passenger_booking"},
            testName = "Verify successful end-to-end booking using data from JSON"
        )
    public void testEndToEndBookingFromJson(Passenger passenger) {
        WebDriver driver = DriverManager.getDriver();
        WebDriverUtils webDriverUtils = new WebDriverUtils(driver, ConfigReader.getPropertyAsInt("test.timeout"));
        driver.get(ConfigReader.getApplicationUrl());
        ExtentTest test = ExtentManager.getTest();

        if (test != null) {
            // --- CHANGE: Using record accessors passenger.firstName() instead of passenger.getFirstName() ---
            test.info("Navigated to: " + ConfigReader.getApplicationUrl());
            test.info("Attempting booking for passenger (JSON): " + passenger.firstName() + " " + passenger.lastName() +
                      " from " + passenger.origin() + " to " + passenger.destination());
        }
        logger.info("Starting flight booking (JSON) for passenger: {} {} from {} to {}",
                    passenger.firstName(), passenger.lastName(), passenger.origin(), passenger.destination());

        HomePage homePage = new HomePage(driver);
        homePage.findFlights(passenger.origin(), passenger.destination());

        boolean urlContainsReserve = webDriverUtils.waitUntilUrlContains("/reserve.php");
        Assert.assertTrue(urlContainsReserve, "Did not navigate to reserve page!");

        FlightSelectionPage flightSelectionPage = new FlightSelectionPage(driver);
        flightSelectionPage.clickChooseFlightButton();

        boolean urlContainsPurchase = webDriverUtils.waitUntilUrlContains("/purchase.php");
        Assert.assertTrue(urlContainsPurchase, "Did not navigate to purchase page!");
        
        // --- ADD THIS LINE TO FORCE A FAILURE ---
//        Assert.assertTrue(false, "Intentionally failing test to check email notification.");

        PurchasePage purchasePage = new PurchasePage(driver);
        purchasePage.fillPurchaseForm(passenger);
        purchasePage.clickPurchaseFlightButton();

        boolean urlContainsConfirmation = webDriverUtils.waitUntilUrlContains("/confirmation.php");
        Assert.assertTrue(urlContainsConfirmation, "Did not navigate to confirmation page after purchase.");

        if (test != null) {
            test.pass("Flight booking (JSON) successful for: " + passenger.firstName() + " " + passenger.lastName());
        }
        logger.info("Flight booking (JSON) completed for passenger: {} {}", passenger.firstName(), passenger.lastName());
    }

    /**
     * Test method to perform flight booking using passenger data from CSV.
     * This test is part of the "regression" and "passenger_booking" groups.
     *
     * @param passenger Passenger record containing personal, payment, and flight details from CSV.
     */
    @Test(dataProvider = "passengerCsvData", dataProviderClass = CsvDataProvider.class,
          groups = {"regression", "passenger_booking"})
    public void testEndToEndBookingFromCsv(Passenger passenger) {
        WebDriver driver = DriverManager.getDriver();
        WebDriverUtils webDriverUtils = new WebDriverUtils(driver, ConfigReader.getPropertyAsInt("test.timeout"));
        driver.get(ConfigReader.getApplicationUrl());
        ExtentTest test = ExtentManager.getTest();

        if (test != null) {
            // --- CHANGE: Using record accessors passenger.firstName() instead of passenger.getFirstName() ---
            test.info("Navigated to: " + ConfigReader.getApplicationUrl());
            test.info("Attempting booking for passenger (CSV): " + passenger.firstName() + " " + passenger.lastName() +
                      " from " + passenger.origin() + " to " + passenger.destination());
        }
        logger.info("Starting flight booking (CSV) for passenger: {} {} from {} to {}",
                    passenger.firstName(), passenger.lastName(), passenger.origin(), passenger.destination());

        HomePage homePage = new HomePage(driver);
        homePage.findFlights(passenger.origin(), passenger.destination());

        boolean urlContainsReserve = webDriverUtils.waitUntilUrlContains("/reserve.php");
        Assert.assertTrue(urlContainsReserve, "Did not navigate to reserve page!");

        FlightSelectionPage flightSelectionPage = new FlightSelectionPage(driver);
        flightSelectionPage.clickChooseFlightButton();

        boolean urlContainsPurchase = webDriverUtils.waitUntilUrlContains("/purchase.php");
        Assert.assertTrue(urlContainsPurchase, "Did not navigate to purchase page!");

        PurchasePage purchasePage = new PurchasePage(driver);
        purchasePage.fillPurchaseForm(passenger);
        purchasePage.clickPurchaseFlightButton();

        boolean urlContainsConfirmation = webDriverUtils.waitUntilUrlContains("/confirmation.php");
        Assert.assertTrue(urlContainsConfirmation, "Did not navigate to confirmation page after purchase.");

        if (test != null) {
            test.pass("Flight booking (CSV) successful for: " + passenger.firstName() + " " + passenger.lastName());
        }
        logger.info("Flight booking (CSV) completed for passenger: {} {}", passenger.firstName(), passenger.lastName());
    }
}