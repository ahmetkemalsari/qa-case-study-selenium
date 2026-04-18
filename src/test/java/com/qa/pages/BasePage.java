package com.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    protected void click(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        scrollIntoView(element);
        try {
            element.click();
        } catch (RuntimeException ex) {
            jsClick(element);
        }
    }

    protected void scrollIntoView(WebElement element) {
        if (driver instanceof JavascriptExecutor js) {
            js.executeScript("arguments[0].scrollIntoView({block:'center', inline:'nearest'});", element);
        }
    }

    protected void jsClick(WebElement element) {
        if (driver instanceof JavascriptExecutor js) {
            js.executeScript("arguments[0].click();", element);
            return;
        }
        element.click();
    }
}

