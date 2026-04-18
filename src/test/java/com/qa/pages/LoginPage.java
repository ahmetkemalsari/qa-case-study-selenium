package com.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage extends BasePage {

    private static final By LOGIN_FORM_CONTAINER = By.cssSelector("form");

    public LoginPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public LoginPage waitUntilLoaded() {
        wait.until(ExpectedConditions.presenceOfElementLocated(LOGIN_FORM_CONTAINER));
        return this;
    }
}

