package com.qa.tests;

import com.qa.pages.ZaraHomePage;
import com.qa.utils.ConfigReader;
import org.junit.jupiter.api.Test;

public class ZaraHomePageTest extends BaseTest {

    @Test
    void shouldOpenZaraTurkeyHomePageAndWaitThirtySeconds() throws InterruptedException {
        String baseUrl = ConfigReader.getProperty("base.url");

        logger.info("Navigating to {}", baseUrl);
        ZaraHomePage homePage = new ZaraHomePage(driver, wait)
                .open(baseUrl)
                .acceptCookiesIfPresent();

        logger.info("Going to login page.");
        homePage.clickLoginButton().waitUntilLoaded();

        logger.info("Waiting on Zara home page for 30 seconds.");
        Thread.sleep(30_000);
    }
}
