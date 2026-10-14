package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.WebDriver;
import support.DriverFactory;
import support.ScreenshotOnFailure;


abstract class BaseTest {

    
    protected WebDriver driver;

    
    @RegisterExtension
    final ScreenshotOnFailure screenshotOnFailure = new ScreenshotOnFailure(() -> driver);

    
    @BeforeEach
    void startBrowser() {
        driver = DriverFactory.create();
    }

    
    @AfterEach
    void stopBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }
}