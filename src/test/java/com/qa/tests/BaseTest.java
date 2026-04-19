package com.qa.tests;

import com.qa.utils.ConfigReader;
import com.qa.utils.LoggerManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

        if (Boolean.parseBoolean(ConfigReader.getProperty("chrome.disable.blink.automation.controlled", "true"))) {
            options.addArguments("--disable-blink-features=AutomationControlled");
        }

        if (Boolean.parseBoolean(ConfigReader.getProperty("chrome.exclude.enable.automation.switch", "true"))) {
            options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
            options.setExperimentalOption("useAutomationExtension", false);
        }

        String userDataDir = ConfigReader.getProperty("chrome.user.data.dir");
        if (userDataDir != null && !userDataDir.isBlank()) {
            String trimmed = userDataDir.trim();
            options.addArguments("--user-data-dir=" + trimmed);
            String profileDir = ConfigReader.getProperty("chrome.profile.directory");
            if (profileDir != null && !profileDir.isBlank()) {
                options.addArguments("--profile-directory=" + profileDir.trim());
            }
            logger.warn(
                    "chrome.user.data.dir aktif: Bu profille normal Chrome acik olmamali; aksi halde oturum cakismasi olur.");
        }

        String userAgent = ConfigReader.getProperty("chrome.user.agent", "");
        if (userAgent != null && !userAgent.isBlank()) {
            options.addArguments("--user-agent=" + userAgent.trim());
        }

        if (headless) {
            options.addArguments("--headless=new");
        }

        ChromeDriver chromeDriver = new ChromeDriver(options);

        if (Boolean.parseBoolean(ConfigReader.getProperty("chrome.override.navigator.webdriver", "true"))) {
            Map<String, Object> params = new HashMap<>();
            params.put("source", "Object.defineProperty(navigator, 'webdriver', { get: () => undefined });");
            chromeDriver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", params);
        }

        chromeDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        return chromeDriver;
    }

    private WebDriverWait createWait(WebDriver driver) {
        long explicitWait = Long.parseLong(ConfigReader.getProperty("explicit.wait"));
        return new WebDriverWait(driver, Duration.ofSeconds(explicitWait));
    }
}
