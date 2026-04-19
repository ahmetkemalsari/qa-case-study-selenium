package com.qa.pages;

import com.qa.testsupport.TestSecrets;
import com.qa.utils.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ZaraHomePage extends BasePage {

    private static final Logger logger = LogManager.getLogger(ZaraHomePage.class);

    private static final By ACCEPT_COOKIES_BUTTON = By.id("onetrust-accept-btn-handler");
    private static final By LOGIN_BUTTON = By.xpath("(//*[@data-qa-id='layout-desktop-actions']//a[@data-qa-id='layout-desktop-layout-logon-action'])[2]");

    public ZaraHomePage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public ZaraHomePage open(String url) {
        driver.get(url);
        waitForDocumentReady();
        if (Boolean.parseBoolean(ConfigReader.getProperty("zara.clear.cookies.after.open", "false"))) {
            driver.manage().deleteAllCookies();
            driver.navigate().refresh();
            waitForDocumentReady();
        }
        injectOneTrustCookiesIfConfigured();
        return this;
    }

    private void waitForDocumentReady() {
        if (driver instanceof JavascriptExecutor js) {
            new WebDriverWait(driver, Duration.ofSeconds(30)).until(webDriver ->
                    "complete".equals(js.executeScript("return document.readyState")));
        }
    }

    /**
     * OneTrust onay çerezleri yapılandırıldıysa eklenir ve sayfa yenilenir; bant görünmeyebilir.
     * Değerler {@code onetrust.harvested.properties} (HarvestOnetrustCookiesTest), config veya ortamdan gelir.
     */
    private void injectOneTrustCookiesIfConfigured() {
        String domain = ConfigReader.getProperty("zara.onetrust.cookie.domain", ".zara.com");
        String path = ConfigReader.getProperty("zara.onetrust.cookie.path", "/");
        String consentName = ConfigReader.getProperty("zara.onetrust.cookie.name.optanonconsent", "OptanonConsent");
        String alertName = ConfigReader.getProperty("zara.onetrust.cookie.name.optanonalertboxclosed", "OptanonAlertBoxClosed");

        String consentValue = TestSecrets.resolveOptional(
                "ZARA_ONETRUST_OPTANONCONSENT",
                "onetrust.optanonconsent",
                ConfigReader.getProperty("zara.onetrust.optanonconsent.value", ""));
        String alertValue = TestSecrets.resolveOptional(
                "ZARA_ONETRUST_OPTANONALERTBOXCLOSED",
                "onetrust.optanonalertboxclosed",
                ConfigReader.getProperty("zara.onetrust.optanonalertboxclosed.value", ""));

        boolean explicitInject = Boolean.parseBoolean(ConfigReader.getProperty("zara.onetrust.inject", "false"));
        boolean hasAny = !consentValue.isEmpty() || !alertValue.isEmpty();
        if (!explicitInject && !hasAny) {
            return;
        }
        if (!hasAny) {
            logger.warn(
                    "zara.onetrust.inject=true ancak Optanon çerez değeri yok; "
                            + "onetrust.harvested.properties uretin (HarvestOnetrustCookiesTest) veya manuel yapılandırın.");
            return;
        }

        try {
            if (!consentValue.isEmpty()) {
                driver.manage().addCookie(new Cookie.Builder(consentName, consentValue).domain(domain).path(path).build());
            }
            if (!alertValue.isEmpty()) {
                driver.manage().addCookie(new Cookie.Builder(alertName, alertValue).domain(domain).path(path).build());
            }
            driver.navigate().refresh();
            waitForDocumentReady();
        } catch (RuntimeException ex) {
            logger.warn("OneTrust çerezleri eklenemedi (domain/path uyumsuzluğu olabilir): {}", ex.toString());
        }
    }

    /**
     * OneTrust bazen gecikmeli gelir; kısa süre bekleyip varsa kabul eder. {@link Thread#sleep} yok.
     */
    public ZaraHomePage acceptCookiesIfPresent() {
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(ExpectedConditions.presenceOfElementLocated(ACCEPT_COOKIES_BUTTON));
            clickElement(btn);
        } catch (TimeoutException ignored) {
            // Çerez bandı yok
        }
        return this;
    }

    public LoginPage clickLoginButton() {
        click(LOGIN_BUTTON);
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(d -> {
            String u = d.getCurrentUrl();
            return u != null && u.contains("account.zara.com");
        });
        return new LoginPage(driver, wait);
    }
}
