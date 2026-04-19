package com.qa.tests;

import com.qa.pages.ZaraHomePage;
import com.qa.testsupport.LoginTestData;
import com.qa.utils.ConfigReader;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class ZaraHomePageTest extends BaseTest {

    @Test
    void shouldOpenZaraTurkeyHomePageAndEnterEmailOnLoginForm() throws InterruptedException {
        String baseUrl = ConfigReader.getProperty("base.url");

        logger.info("Navigating to {}", baseUrl);
        ZaraHomePage homePage = new ZaraHomePage(driver, wait)
                .open(baseUrl)
                .acceptCookiesIfPresent();

        logger.info("Going to login page.");
        homePage.clickLoginButton()
                .waitUntilLoaded()
                .dismissAlertDialogsUntilGone()
                .enterEmail(LoginTestData.email())
                .clickEmailStepContinue()
                .dismissAlertDialogsUntilGone();

        logger.info("E-posta girildi, devam tiklandi.");
        logger.info("60 sn bekleniyor...");
        Thread.sleep(Duration.ofSeconds(120).toMillis());
    }
}
