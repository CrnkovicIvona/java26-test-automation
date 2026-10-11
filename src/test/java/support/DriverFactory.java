package support;

import config.TestConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Map;

/**
 * Creates Chrome WebDriver sessions configured consistently for local and CI tests.
 */
public final class DriverFactory {

    /**
     * Builds a Chrome session using the shared headless setting and disables
     * browser credential prompts that could interfere with UI automation.
     *
     * @return a newly created Chrome WebDriver session
     */
    public static WebDriver create() {
        var options = new ChromeOptions();
        // Use a stable viewport so catalog layout and screenshots are predictable.
        options.addArguments("--window-size=1920,1080");
        if (TestConfig.HEADLESS) {
            // Use Chrome's current headless implementation when requested.
            options.addArguments("--headless=new");
        }
        // Disable password-manager features that can overlay or alter the login form.
        options.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false));
        return new ChromeDriver(options);
    }

    /** Prevents instances of this factory-only utility class. */
    private DriverFactory() {}
}