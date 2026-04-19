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

    /**
     * Locator ile eşleşen element DOM'da oluşana kadar bekler, sonra tıklar (scroll + normal click, gerekirse JS).
     * Bazı sitelerde {@link ExpectedConditions#visibilityOfElementLocated(By)} veya
     * {@link ExpectedConditions#elementToBeClickable(By)} gereksiz sıkı kalır (header/link SPA yüzünden);
     * bu yüzden varsayılan olarak önce {@code presence} kullanılır.
     * {@link Thread#sleep} kullanılmaz.
     */
    protected void click(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        clickElement(element);
    }

    protected void clickElement(WebElement element) {
        RuntimeException lastFailure = null;

        for (int attempt = 0; attempt < 2; attempt++) {
            scrollIntoView(element);

            try {
                element.click();
                return;
            } catch (RuntimeException ex) {
                lastFailure = ex;
                try {
                    jsClick(element);
                    return;
                } catch (RuntimeException jsEx) {
                    lastFailure = jsEx;
                }
            }
        }

        if (lastFailure != null) {
            throw lastFailure;
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
