package support;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Supplier;

/**
 * JUnit extension that saves a browser screenshot when a test method fails.
 *
 * <p>The WebDriver is obtained lazily because {@code BaseTest} initializes the
 * browser immediately before each test. Screenshots are written to
 * {@code target/screenshots} for collection by Maven and CI.
 */
public class ScreenshotOnFailure implements AfterTestExecutionCallback {

    /** Supplies the active test's browser session when the callback runs. */
    private final Supplier<WebDriver> driver;

    /**
     * Creates the extension with a supplier for the current test's WebDriver.
     *
     * @param driver supplier that returns the active browser session
     */
    public ScreenshotOnFailure(Supplier<WebDriver> driver) {
        this.driver = driver;
    }

    /**
     * Captures and saves a PNG only when the test failed and its browser session
     * is still available.
     *
     * @param context JUnit metadata for the completed test method
     * @throws Exception if Selenium cannot capture the screenshot or the file
     *                   cannot be created or copied
     */
    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        if (context.getExecutionException().isEmpty() || driver.get() == null) {
            return;
        }
        // Ask the active browser to write the screenshot to a temporary file.
        var file = ((TakesScreenshot) driver.get()).getScreenshotAs(OutputType.FILE);
        // Keep generated evidence under Maven's standard build-output directory.
        var dir = Path.of("target", "screenshots");
        Files.createDirectories(dir);
        // Include the test class, method, and time to distinguish separate failures.
        var name = context.getRequiredTestClass().getSimpleName() + "_"
                + context.getRequiredTestMethod().getName() + "_"
                + System.currentTimeMillis() + ".png";
        Files.copy(file.toPath(), dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
    }
}