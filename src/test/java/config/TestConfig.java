package config;

import java.time.Duration;

public final class TestConfig {

    public static final String BASE_URL = "https://www.saucedemo.com/";

    public static final Duration TIMEOUT = Duration.ofSeconds(10);

    public static final boolean HEADLESS = Boolean.getBoolean("headless");

    private TestConfig() {}
}