package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.WebDriver;
import support.DriverFactory;
import support.ScreenshotOnFailure;

/**
 * Provides browser setup, cleanup, and failure screenshots shared by UI tests.
 *
 * <p>Each test receives its own WebDriver session so browser state does not
 * leak between tests. The registered extension captures a screenshot after a
 * failure and before browser teardown.
 */
abstract class BaseTest {

    /** Browser session used by the current test and its page objects. */
    protected WebDriver driver;

    /** JUnit extension that captures a screenshot when a test fails. */
    @RegisterExtension
    final ScreenshotOnFailure screenshotOnFailure = new ScreenshotOnFailure(() -> driver);

    /** Creates a fresh browser session before each test starts. */
    @BeforeEach
    void startBrowser() {
        driver = DriverFactory.create();
    }

    /** Closes the browser after each test to release local and CI resources. */
    @AfterEach
    void stopBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }
}