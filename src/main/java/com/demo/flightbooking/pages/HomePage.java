package com.demo.flightbooking.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select; // Keep Select for direct dropdown interaction if needed for options retrieval

import com.demo.flightbooking.utils.ConfigReader;
import com.demo.flightbooking.utils.WebDriverUtils; // Import WebDriverUtils

import java.util.List;
import java.util.stream.Collectors;

/**
 * Page Object for the Home Page (blazedemo.com).
 * This class contains elements and actions related to the flight search form.
 */
public class HomePage extends BasePage { // Extend BasePage

    // We no longer need to initialize WebDriverWait here, it's in BasePage
    // private WebDriverWait wait;

    // --- Locators for elements on the Home Page ---
    private By departFromDropdown = By.name("fromPort");
    private By arriveAtDropdown = By.name("toPort");
    private By findFlightsButton = By.cssSelector("input[type='submit']");

    // WebDriverUtils instance for robust interactions
    private WebDriverUtils webDriverUtils; // Declare WebDriverUtils

    // Constructor to initialize the WebDriver, WebDriverWait (via BasePage), and WebDriverUtils
    public HomePage(WebDriver driver) {
        super(driver); // Call BasePage constructor
        // Initialize WebDriverUtils with the driver and the same timeout from ConfigReader
        this.webDriverUtils = new WebDriverUtils(driver, ConfigReader.getPropertyAsInt("test.timeout"));
        logger.info("HomePage initialized."); // Use the logger from BasePage
    }

    /**
     * Retrieves a list of available departure cities from the dropdown.
     *
     * @return A list of strings representing the available departure cities.
     */
    public List<String> getAvailableDepartCities() {
        logger.debug("Getting available departure cities.");
        WebElement departFromElement = webDriverUtils.findElement(departFromDropdown); // Use WebDriverUtils
        Select select = new Select(departFromElement);
        return select.getOptions().stream()
                    .map(WebElement::getText)
                    .collect(Collectors.toList());
    }

    /**
     * Selects the departure city from the dropdown.
     *
     * @param city The city to select (e.g., "Paris", "Boston").
     */
    public void selectDepartFromCity(String city) {
        logger.info("Selecting departure city: {}", city);
        // Use the selectByVisibleText method from WebDriverUtils
        webDriverUtils.selectByVisibleText(departFromDropdown, city);
    }

    /**
     * Retrieves a list of available arrival cities from the dropdown.
     *
     * @return A list of strings representing the available arrival cities.
     */
    public List<String> getAvailableArriveCities() {
        logger.debug("Getting available arrival cities.");
        WebElement arriveAtElement = webDriverUtils.findElement(arriveAtDropdown); // Use WebDriverUtils
        Select select = new Select(arriveAtElement);
        return select.getOptions().stream()
                    .map(WebElement::getText)
                    .collect(Collectors.toList());
    }

    /**
     * Selects the arrival city from the dropdown.
     *
     * @param city The city to select (e.g., "London", "Rome").
     */
    public void selectArriveAtCity(String city) {
        logger.info("Selecting arrival city: {}", city);
        // Use the selectByVisibleText method from WebDriverUtils
        webDriverUtils.selectByVisibleText(arriveAtDropdown, city);
    }

    /**
     * Clicks the "Find Flights" button to proceed to the flight selection page.
     */
    public void clickFindFlightsButton() {
        logger.info("Clicking Find Flights button.");
        // Use the click method from WebDriverUtils
        webDriverUtils.click(findFlightsButton);
    }

    /**
     * Performs the full flight search operation from the home page.
     *
     * @param departCity The city to depart from.
     * @param arriveCity The city to arrive at.
     */
    public void findFlights(String departCity, String arriveCity) {
        logger.info("Performing flight search from {} to {}.", departCity, arriveCity);
        selectDepartFromCity(departCity);
        selectArriveAtCity(arriveCity);
        clickFindFlightsButton();
        logger.info("Flight search initiated.");
    }
}