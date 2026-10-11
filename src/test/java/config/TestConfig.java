package config;

import java.time.Duration;

/**
 * Stores shared configuration values used by browser tests and page objects.
 *
 * <p>Headless execution is controlled with {@code -Dheadless=true}, allowing
 * local and CI runs to use the same test code.
 */
public final class TestConfig {

    /** Root address of the public SauceDemo application under test. */
    public static final String BASE_URL = "https://www.saucedemo.com/";

    /** Maximum wait applied to explicit Selenium conditions in page objects. */
    public static final Duration TIMEOUT = Duration.ofSeconds(10);

    /** Whether Chrome should run without a visible browser window. */
    public static final boolean HEADLESS = Boolean.getBoolean("headless");

    /** Prevents instances of this constants-only utility class. */
    private TestConfig() {}
}