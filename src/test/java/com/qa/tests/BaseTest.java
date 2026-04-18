package com.qa.tests;

import com.qa.utils.ConfigReader;
import com.qa.utils.LoggerManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

public abstract class BaseTest {

    protected final Logger logger = LoggerManager.getLogger(getClass());
    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeEach
    void setUpTest() {
        logger.info("Test setup started.");
        driver = createDriver();
        wait = createWait(driver);
    }

    @AfterEach
    void tearDownTest() {
        if (driver != null) {
            driver.quit();
        }

        logger.info("Test teardown completed.");
    }

    private WebDriver createDriver() {
        String browser = ConfigReader.getProperty("browser");
        boolean headless = Boolean.parseBoolean(ConfigReader.getProperty("headless"));
        long implicitWait = Long.parseLong(ConfigReader.getProperty("implicit.wait"));

        if (!"chrome".equalsIgnoreCase(browser)) {
            throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");

        if (headless) {
            options.addArguments("--headless=new");
        }

        WebDriver webDriver = new ChromeDriver(options);
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        return webDriver;
    }

    private WebDriverWait createWait(WebDriver driver) {
        long explicitWait = Long.parseLong(ConfigReader.getProperty("explicit.wait"));
        return new WebDriverWait(driver, Duration.ofSeconds(explicitWait));
    }
}
