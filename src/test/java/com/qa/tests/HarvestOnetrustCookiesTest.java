package com.qa.tests;

import com.qa.pages.ZaraHomePage;
import com.qa.testsupport.TestSecrets;
import com.qa.utils.ConfigReader;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openqa.selenium.Cookie;

import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Çerez değerlerini elle kopyalamak yerine: tarayıcıda bandı kabul edip Optanon çerezlerini dosyaya yazar.
 * <p>
 * Çalıştırma (proje kökünden):
 * {@code mvn test -Dharvest.onetrust=true -Dtest=HarvestOnetrustCookiesTest}
 * <p>
 * Çıktı: {@code src/test/resources/onetrust.harvested.properties} (gitignore) ve aynı oturumda
 * {@code target/test-classes} kopyası. Sonraki normal testler bu dosyadan enjekte eder.
 */
@Order(1)
public class HarvestOnetrustCookiesTest extends BaseTest {

    @Test
    @EnabledIfSystemProperty(named = "harvest.onetrust", matches = "true")
    void writeHarvestedOnetrustPropertiesFile() throws Exception {
        String baseUrl = ConfigReader.getProperty("base.url");
        new ZaraHomePage(driver, wait).open(baseUrl).acceptCookiesIfPresent();

        Properties out = new Properties();
        Cookie consent = driver.manage().getCookieNamed("OptanonConsent");
        if (consent != null) {
            out.setProperty("onetrust.optanonconsent", consent.getValue());
        }
        Cookie alertClosed = driver.manage().getCookieNamed("OptanonAlertBoxClosed");
        if (alertClosed != null) {
            out.setProperty("onetrust.optanonalertboxclosed", alertClosed.getValue());
        }

        if (out.isEmpty()) {
            throw new IllegalStateException(
                    "OptanonConsent / OptanonAlertBoxClosed bulunamadı. Çerez bandı görünmediyse bekleme süresini "
                            + "veya base.url değerini kontrol edin.");
        }

        Path projectRoot = Path.of(System.getProperty("user.dir"));
        Path srcOut = projectRoot.resolve("src/test/resources/onetrust.harvested.properties");
        Path targetOut = projectRoot.resolve("target/test-classes/onetrust.harvested.properties");

        Files.createDirectories(srcOut.getParent());
        try (Writer w = Files.newBufferedWriter(srcOut, StandardCharsets.UTF_8)) {
            out.store(w, "HarvestOnetrustCookiesTest; gitignore — manuel düzenlemeyin");
        }

        Files.createDirectories(targetOut.getParent());
        try (Writer w = Files.newBufferedWriter(targetOut, StandardCharsets.UTF_8)) {
            out.store(w, "HarvestOnetrustCookiesTest; gitignore — manuel düzenlemeyin");
        }

        TestSecrets.invalidateHarvestedOnetrustCache();

        logger.info("OneTrust çerezleri yazıldı: {} (ve target/test-classes kopyası)", srcOut.toAbsolutePath());
    }
}
