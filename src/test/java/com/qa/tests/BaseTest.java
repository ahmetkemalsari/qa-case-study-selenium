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
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
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
        String browser = resolveBrowserName();
        boolean headless = Boolean.parseBoolean(ConfigReader.getProperty("headless"));
        long implicitWait = Long.parseLong(ConfigReader.getProperty("implicit.wait"));

        WebDriver webDriver;
        if ("chrome".equalsIgnoreCase(browser)) {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized");
            applyChromeAntiAutomationOptions(options);
            options.addArguments("--user-agent=" + resolveUserAgent("chrome"));
            if (headless) {
                options.addArguments("--headless=new");
            }
            ChromeDriver chromeDriver = new ChromeDriver(options);
            injectChromeNavigatorWebdriverPatch(chromeDriver);
            webDriver = chromeDriver;
        } else if ("firefox".equalsIgnoreCase(browser)) {
            WebDriverManager.firefoxdriver().setup();
            FirefoxOptions options = new FirefoxOptions();
            String firefoxBinary = ConfigReader.getProperty("firefox.binary", "").trim();
            if (!firefoxBinary.isEmpty()) {
                options.setBinary(firefoxBinary);
            }
            options.addPreference("general.useragent.override", resolveUserAgent("firefox"));
            options.addPreference("dom.webdriver.enabled", false);
            if (headless) {
                options.addArguments("-headless");
            }
            webDriver = new FirefoxDriver(options);
            webDriver.manage().window().maximize();
        } else {
            throw new IllegalArgumentException("Unsupported browser: " + browser + " (chrome veya firefox)");
        }

        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        return webDriver;
    }

    private WebDriverWait createWait(WebDriver driver) {
        long explicitWait = Long.parseLong(ConfigReader.getProperty("explicit.wait"));
        return new WebDriverWait(driver, Duration.ofSeconds(explicitWait));
    }

    /** Ornek: {@code mvn test -Dbrowser=chrome} (config.properties uzerine yazar). */
    private static String resolveBrowserName() {
        String fromJvm = System.getProperty("browser");
        if (fromJvm != null && !fromJvm.isBlank()) {
            return fromJvm.trim();
        }
        return ConfigReader.getProperty("browser");
    }

    private static void applyChromeAntiAutomationOptions(ChromeOptions options) {
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-infobars");
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
    }

    /**
     * navigator.webdriver bayragini gizlemeye calisir (Chrome CDP); bot tespitini zorlastirir.
     */
    private static void injectChromeNavigatorWebdriverPatch(ChromeDriver driver) {
        Map<String, Object> params = new HashMap<>();
        params.put("source", "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
        driver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", params);
    }

    /**
     * Bos ise guncel Windows + tarayici UA; {@code browser.user.agent} ile configden ezilebilir.
     */
    private static String resolveUserAgent(String browserKind) {
        String custom = ConfigReader.getProperty("browser.user.agent", "").trim();
        if (!custom.isEmpty()) {
            return custom;
        }
        if ("chrome".equalsIgnoreCase(browserKind)) {
            return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";
        }
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:133.0) Gecko/20100101 Firefox/133.0";
    }
}
