package com.qa.pages;

import com.qa.testsupport.HumanInteraction;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ZaraHomePage extends BasePage {

    private static final By ACCEPT_COOKIES_BUTTON = By.id("onetrust-accept-btn-handler");
    private static final By LOGIN_BUTTON = By.xpath("(//*[@data-qa-id='layout-desktop-actions']//a[@data-qa-id='layout-desktop-layout-logon-action'])[2]");

    public ZaraHomePage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public ZaraHomePage open(String url) {
        driver.get(url);
        HumanInteraction.pauseBetweenActions();
        return this;
    }

    public ZaraHomePage acceptCookiesIfPresent() {
        if (!driver.findElements(ACCEPT_COOKIES_BUTTON).isEmpty()) {
            click(ACCEPT_COOKIES_BUTTON);
        }
        return this;
    }

    public LoginPage clickLoginButton() {
        click(LOGIN_BUTTON);
        return new LoginPage(driver, wait);
    }
}

