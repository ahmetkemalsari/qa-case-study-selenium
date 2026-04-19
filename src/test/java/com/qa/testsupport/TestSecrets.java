package com.qa.testsupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class TestSecrets {

    private static final String LOCAL_SECRETS_FILE = "test.local.properties";
    private static final String HARVESTED_ONETRUST_FILE = "onetrust.harvested.properties";

    private static final String ENV_LOGIN_EMAIL = "ZARA_TEST_EMAIL";
    private static final String PROP_LOGIN_EMAIL = "login.email";

    private static volatile Properties localProperties;
    private static volatile Properties harvestedOnetrustProperties;

    private TestSecrets() {
    }

    public static String loginEmail() {
        return getRequired(ENV_LOGIN_EMAIL, PROP_LOGIN_EMAIL);
    }

    /** HarvestOnetrustCookiesTest dosyayı yazdıktan sonra aynı JVM içindeki testlerin yeniden okuması için. */
    public static void invalidateHarvestedOnetrustCache() {
        harvestedOnetrustProperties = null;
    }

    /**
     * Sıra: ortam değişkeni → JVM {@code -DpropertyKey=} → {@code test.local.properties} →
     * {@code onetrust.harvested.properties} (HarvestOnetrustCookiesTest ile üretilir) → {@code configFallback}.
     */
    public static String resolveOptional(String envKey, String propertyKey, String configFallback) {
        String fromEnv = System.getenv(envKey);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }

        String fromSysProp = System.getProperty(propertyKey);
        if (fromSysProp != null && !fromSysProp.isBlank()) {
            return fromSysProp.trim();
        }

        String fromFile = loadLocalProperties().getProperty(propertyKey);
        if (fromFile != null && !fromFile.isBlank()) {
            return fromFile.trim();
        }

        String fromHarvested = loadHarvestedOnetrustProperties().getProperty(propertyKey);
        if (fromHarvested != null && !fromHarvested.isBlank()) {
            return fromHarvested.trim();
        }

        if (configFallback == null) {
            return "";
        }
        String trimmed = configFallback.trim();
        return trimmed;
    }

    private static String getRequired(String envKey, String propertyKey) {
        String fromEnv = System.getenv(envKey);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }

        String fromSysProp = System.getProperty(propertyKey);
        if (fromSysProp != null && !fromSysProp.isBlank()) {
            return fromSysProp.trim();
        }

        String fromFile = loadLocalProperties().getProperty(propertyKey);
        if (fromFile != null && !fromFile.isBlank()) {
            return fromFile.trim();
        }

        throw new IllegalStateException(
                "Missing secret/config value. Set environment variable '" + envKey + "', "
                        + "or JVM system property '-D" + propertyKey + "=...', "
                        + "or add '" + propertyKey + "' to classpath file '" + LOCAL_SECRETS_FILE + "' "
                        + "(see src/test/resources/test.local.properties.example)."
        );
    }

    private static Properties loadLocalProperties() {
        Properties cached = localProperties;
        if (cached != null) {
            return cached;
        }

        synchronized (TestSecrets.class) {
            if (localProperties != null) {
                return localProperties;
            }

            Properties properties = new Properties();
            try (InputStream inputStream = TestSecrets.class.getClassLoader().getResourceAsStream(LOCAL_SECRETS_FILE)) {
                if (inputStream != null) {
                    try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                        properties.load(reader);
                    }
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to load local secrets file: " + LOCAL_SECRETS_FILE, exception);
            }

            localProperties = properties;
            return localProperties;
        }
    }

    private static Properties loadHarvestedOnetrustProperties() {
        Properties cached = harvestedOnetrustProperties;
        if (cached != null) {
            return cached;
        }

        synchronized (TestSecrets.class) {
            if (harvestedOnetrustProperties != null) {
                return harvestedOnetrustProperties;
            }

            Properties properties = new Properties();
            try (InputStream inputStream = TestSecrets.class.getClassLoader().getResourceAsStream(HARVESTED_ONETRUST_FILE)) {
                if (inputStream != null) {
                    try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                        properties.load(reader);
                    }
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to load file: " + HARVESTED_ONETRUST_FILE, exception);
            }

            harvestedOnetrustProperties = properties;
            return harvestedOnetrustProperties;
        }
    }
}
