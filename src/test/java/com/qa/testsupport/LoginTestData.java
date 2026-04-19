package com.qa.testsupport;

import com.qa.utils.ConfigReader;

/**
 * Giris testlerinde kullanilacak e-posta; hassas degerleri repoda tutmamak icin
 * once ortam degiskeni, yoksa {@code config.properties} okunur.
 */
public final class LoginTestData {

    public static final String ENV_LOGIN_EMAIL = "ZARA_LOGIN_EMAIL";

    private LoginTestData() {
    }

    public static String email() {
        String fromEnv = System.getenv(ENV_LOGIN_EMAIL);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        String fromConfig = ConfigReader.getProperty("login.email", "").trim();
        if (!fromConfig.isEmpty()) {
            return fromConfig;
        }
        throw new IllegalStateException(
                "Test e-postasi tanimli degil. Ortam degiskeni " + ENV_LOGIN_EMAIL
                        + " ayarlayin veya src/main/resources/config.properties icinde login.email girin.");
    }
}
