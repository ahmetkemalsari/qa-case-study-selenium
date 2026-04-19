package com.qa.testsupport;

import com.qa.utils.ConfigReader;
import org.openqa.selenium.WebElement;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Rastgele gecikmeler ve karakter karakter yazim; bot trafiginden ayirmaya yardim eder.
 * {@code automation.human.behavior=false} ile kapatilabilir.
 */
public final class HumanInteraction {

    private HumanInteraction() {
    }

    public static boolean isHumanBehaviorEnabled() {
        return Boolean.parseBoolean(ConfigReader.getProperty("automation.human.behavior", "true"));
    }

    public static void pauseBetweenActions() {
        if (!isHumanBehaviorEnabled()) {
            return;
        }
        int min = Integer.parseInt(ConfigReader.getProperty("human.pause.min.ms", "80"));
        int max = Integer.parseInt(ConfigReader.getProperty("human.pause.max.ms", "350"));
        sleepMs(randomBetween(min, max));
    }

    public static void pauseShort() {
        if (!isHumanBehaviorEnabled()) {
            return;
        }
        sleepMs(randomBetween(40, 180));
    }

    public static void typeLikeHuman(WebElement field, String text) {
        field.clear();
        if (!isHumanBehaviorEnabled()) {
            field.sendKeys(text);
            return;
        }
        int min = Integer.parseInt(ConfigReader.getProperty("human.typing.min.ms", "35"));
        int max = Integer.parseInt(ConfigReader.getProperty("human.typing.max.ms", "120"));
        for (char ch : text.toCharArray()) {
            field.sendKeys(String.valueOf(ch));
            sleepMs(randomBetween(min, max));
        }
    }

    private static int randomBetween(int minInclusive, int maxInclusive) {
        if (maxInclusive <= minInclusive) {
            return minInclusive;
        }
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }

    private static void sleepMs(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Insansi bekleme kesildi.", e);
        }
    }
}
