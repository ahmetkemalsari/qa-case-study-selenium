package com.qa.tests;

import com.qa.pages.ZaraHomePage;
import com.qa.testsupport.TestSecrets;
import com.qa.utils.ConfigReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(2)
public class ZaraHomePageTest extends BaseTest {

    @Test
    void shouldOpenZaraTurkeyHomePageAndContinueLoginFlow() {
        String baseUrl = ConfigReader.getProperty("base.url");

        logger.info("Navigating to {}", baseUrl);
        var loginPage = new ZaraHomePage(driver, wait)
                .open(baseUrl)
                .acceptCookiesIfPresent()
                .clickLoginButton()
                .waitUntilLoaded();

        loginPage.enterEmail(TestSecrets.loginEmail());
        loginPage.submitEmailWithEnter();

        if (loginPage.waitForGenericServerErrorModal(15)) {
            logger.error(
                    "Zara sunucu hatası modalı göründü (MAALESEF...). OK tıklaması modalı kapatır; giriş akışı sunucu reddi nedeniyle ilerlemez.");
            loginPage.clickOkIfPresent();
            Assertions.fail(
                    "Zara 'Maalesef bir sorun oluştu' modalı: istek sunucuda reddedildi veya hata döndü. "
                            + "Otomasyon ile düzeltilemez; farklı zaman/ağ/oturum ile tekrar deneyin veya manuel doğrulayın.");
        }

        loginPage.clickOkIfPresent();
        Assertions.assertTrue(
                loginPage.continueToPasswordStep(20),
                "E-posta adimindan sonra sifre ekranina veya sifre secenegine ulasilamadi.");
    }
}
