package com.qa.pages;

import com.qa.utils.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class LoginPage extends BasePage {

    private static final By LOGIN_FORM_CONTAINER = By.cssSelector("form");
    private static final By USERNAME_INPUT = By.xpath("//input[@name='username']");
    private static final By CONTINUE_BUTTON = By.xpath("//button[@data-qa-id='logon-form-submit']");
    private static final By PASSWORD_INPUT = By.xpath(
            "//input[@type='password' or @name='password' or contains(@autocomplete,'current-password')]");
    /**
     * "OK" / "Ok" — modal metni büyük harf olabiliyor; {@code text()='Ok'} tek başına eşleşmeyebilir.
     */
    private static final By OK_BUTTON = By.xpath(
            "//button[@type='button' and translate(normalize-space(),"
                    + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')='ok']");
    private static final By ALTERNATIVE_LOGIN_METHODS = By.xpath(
            "//*[contains(@class,'alternative-login-methods')]//*[self::button or self::a or @role='button']"
                    + " | //*[contains(@class,'alternative-login-methods')]");
    private static final List<By> PASSWORD_STEP_TRIGGERS = List.of(
            CONTINUE_BUTTON,
            ALTERNATIVE_LOGIN_METHODS,
            By.xpath("//button[contains(@data-qa-id,'password')]"),
            By.xpath("//a[contains(@data-qa-id,'password')]"),
            By.xpath("//button[contains(translate(normalize-space(.),"
                    + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZÇĞİÖŞÜ', 'abcdefghijklmnopqrstuvwxyzçğiöşü'),'devam')]"),
            By.xpath("//button[contains(translate(normalize-space(.),"
                    + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZÇĞİÖŞÜ', 'abcdefghijklmnopqrstuvwxyzçğiöşü'),'continue')]"),
            By.xpath("//button[contains(translate(normalize-space(.),"
                    + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZÇĞİÖŞÜ', 'abcdefghijklmnopqrstuvwxyzçğiöşü'),'password')]"),
            By.xpath("//button[contains(translate(normalize-space(.),"
                    + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZÇĞİÖŞÜ', 'abcdefghijklmnopqrstuvwxyzçğiöşü'),'şifre')]"),
            By.xpath("//a[contains(translate(normalize-space(.),"
                    + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZÇĞİÖŞÜ', 'abcdefghijklmnopqrstuvwxyzçğiöşü'),'şifre')]")
    );
    private static final By ERROR_MODAL_HEADING = By.xpath("//*[contains(., 'MAALESEF')]");

    public LoginPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public LoginPage waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_FORM_CONTAINER));
        return this;
    }

    public LoginPage enterEmail(String email) {
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));
        scrollIntoView(username);
        username.click();
        username.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        username.sendKeys(Keys.DELETE);
        int delayPerChar = parsePositiveIntOrZero(ConfigReader.getProperty("human.type.delay.ms.per.char"));
        if (delayPerChar > 0) {
            for (int i = 0; i < email.length(); i++) {
                username.sendKeys(String.valueOf(email.charAt(i)));
                sleepMs(delayPerChar);
            }
        } else {
            username.sendKeys(email);
        }
        return this;
    }

    /**
     * "Devam et" yerine e-posta alanında Enter — bazı formlar submit'i böyle tetikler.
     * Enter'dan önce {@code human.pause.after.email.ms} ile kısa insani bekleme.
     */
    public LoginPage submitEmailWithEnter() {
        pauseAfterEmailBeforeEnter();
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));
        scrollIntoView(username);
        username.sendKeys(Keys.ENTER);
        return this;
    }

    public LoginPage clickContinue() {
        click(CONTINUE_BUTTON);
        return this;
    }

    public LoginPage clickOkIfPresent() {
        try {
            WebElement ok = new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(ExpectedConditions.elementToBeClickable(OK_BUTTON));
            clickElement(ok);
        } catch (TimeoutException ignored) {
            // OK yok veya henüz tıklanabilir değil
        }
        return this;
    }

    /**
     * Enter / devam sonrası "MAALESEF..." metni DOM'da belirene kadar bekler (gecikmeli modal için).
     *
     * @return süre içinde göründüyse true
     */
    public boolean waitForGenericServerErrorModal(int maxWaitSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(maxWaitSeconds))
                    .until(ExpectedConditions.presenceOfElementLocated(ERROR_MODAL_HEADING));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * @deprecated Anlık kontrol; modal gecikirse false döner. {@link #waitForGenericServerErrorModal(int)} kullanın.
     */
    @Deprecated
    public boolean isGenericServerErrorModalVisible() {
        return !driver.findElements(ERROR_MODAL_HEADING).isEmpty();
    }

    public boolean continueToPasswordStep(int maxWaitSeconds) {
        if (isPasswordInputVisible()) {
            return true;
        }

        try {
            return new WebDriverWait(driver, Duration.ofSeconds(maxWaitSeconds)).until(d -> {
                if (isPasswordInputVisible()) {
                    return true;
                }

                WebElement trigger = findFirstDisplayed(PASSWORD_STEP_TRIGGERS);
                if (trigger == null) {
                    return false;
                }

                clickElement(trigger);
                return isPasswordInputVisible();
            });
        } catch (TimeoutException e) {
            return false;
        }
    }

    public LoginPage clickPasswordLoginAlternative() {
        if (!continueToPasswordStep(15)) {
            throw new TimeoutException("Password login alternative could not be reached.");
        }
        return this;
    }

    private void pauseAfterEmailBeforeEnter() {
        String raw = ConfigReader.getProperty("human.pause.after.email.ms");
        long ms = 600;
        if (raw != null && !raw.isBlank()) {
            try {
                ms = Long.parseLong(raw.trim());
            } catch (NumberFormatException ignored) {
                ms = 600;
            }
        }
        sleepMs(ms);
    }

    private static int parsePositiveIntOrZero(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(raw.trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static void sleepMs(long ms) {
        if (ms <= 0) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isPasswordInputVisible() {
        return !driver.findElements(PASSWORD_INPUT).isEmpty();
    }

    private WebElement findFirstDisplayed(List<By> locators) {
        for (By locator : locators) {
            List<WebElement> elements = driver.findElements(locator);
            for (WebElement element : elements) {
                try {
                    if (element.isDisplayed()) {
                        return element;
                    }
                } catch (RuntimeException ignored) {
                    // DOM degismisse siradaki adaya bak.
                }
            }
        }
        return null;
    }
}
