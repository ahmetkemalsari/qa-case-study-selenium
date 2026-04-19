package com.qa.pages;

import com.qa.utils.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class LoginPage extends BasePage {

    private static final String ACCOUNT_HOST = "account.zara.com";

    private static final By EMAIL_INPUT = By.cssSelector("input[type='email']");
    private static final By EMAIL_STEP_CONTINUE = By.xpath("//button[@data-qa-id='logon-form-submit']");

    private static final By ALERT_DIALOG = By.xpath("//*[@role='alertdialog']");
    private static final By ALERT_DIALOG_BUTTON = By.xpath("//*[@role='alertdialog']//button");

    public LoginPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    /**
     * Once account giris URL'sine veya dogru sekmeye gecilir; gerekirse iframe icinde e-posta alani aranir.
     * Sadece {@code form} beklemek ana sayfadaki baska formlarla karisabiliyordu.
     */
    public LoginPage waitUntilLoaded() {
        waitForAccountLoginContext();
        ensureEmailFieldInFrameOrDocument();
        long sec = Long.parseLong(ConfigReader.getProperty("explicit.wait"));
        WebDriverWait fieldWait = new WebDriverWait(driver, Duration.ofSeconds(Math.max(sec, 25)));
        fieldWait.until(ExpectedConditions.elementToBeClickable(EMAIL_INPUT));
        return this;
    }

    public LoginPage enterEmail(String email) {
        clearAndType(EMAIL_INPUT, email);
        return this;
    }

    /** E-posta girildikten sonraki "Devam" / submit adimi. */
    public LoginPage clickEmailStepContinue() {
        click(EMAIL_STEP_CONTINUE);
        return this;
    }

    /**
     * "Aller" vb. {@code alertdialog} tekrar tekrar gelebilir; kalmayana kadar icindeki butona (OK) tiklar.
     */
    public LoginPage dismissAlertDialogsUntilGone() {
        final int maxAttempts = 40;
        WebDriverWait quickWait = new WebDriverWait(driver, Duration.ofSeconds(5));
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            if (driver.findElements(ALERT_DIALOG).isEmpty()) {
                break;
            }
            try {
                WebElement button = quickWait.until(ExpectedConditions.elementToBeClickable(ALERT_DIALOG_BUTTON));
                scrollIntoView(button);
                button.click();
            } catch (TimeoutException e) {
                break;
            } catch (StaleElementReferenceException e) {
                // DOM guncellendi; dongu tekrar dener
            }
        }
        return this;
    }

    private void waitForAccountLoginContext() {
        long sec = Long.parseLong(ConfigReader.getProperty("explicit.wait"));
        WebDriverWait navigationWait = new WebDriverWait(driver, Duration.ofSeconds(Math.max(sec, 25)));
        navigationWait.until(d -> switchToAccountWindowIfAvailable());
    }

    /** account.zara.com iceren pencereye gecer; ayni sekmede yuklenmisse URL yakalanir. */
    private boolean switchToAccountWindowIfAvailable() {
        for (String handle : driver.getWindowHandles()) {
            driver.switchTo().window(handle);
            String url = safeCurrentUrl();
            if (url.contains(ACCOUNT_HOST)) {
                return true;
            }
        }
        return false;
    }

    private String safeCurrentUrl() {
        try {
            String u = driver.getCurrentUrl();
            return u != null ? u : "";
        } catch (Exception e) {
            return "";
        }
    }

    /** E-posta alani ana dokumanda yoksa iframe icinde aranir. */
    private void ensureEmailFieldInFrameOrDocument() {
        driver.switchTo().defaultContent();
        if (hasInteractableEmailInput()) {
            return;
        }
        List<WebElement> frames = driver.findElements(By.tagName("iframe"));
        for (WebElement frame : frames) {
            driver.switchTo().defaultContent();
            driver.switchTo().frame(frame);
            if (hasInteractableEmailInput()) {
                return;
            }
        }
        driver.switchTo().defaultContent();
    }

    private boolean hasInteractableEmailInput() {
        List<WebElement> inputs = driver.findElements(EMAIL_INPUT);
        for (WebElement input : inputs) {
            try {
                if (input.isDisplayed() && input.isEnabled()) {
                    return true;
                }
            } catch (Exception ignored) {
                // stale vb.
            }
        }
        return false;
    }
}
