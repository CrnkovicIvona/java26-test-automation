package support;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.WebDriver;

import java.util.function.Supplier;


public class ScreenshotOnFailure implements AfterTestExecutionCallback {

    
    private final Supplier<WebDriver> driver;

    
    public ScreenshotOnFailure(Supplier<WebDriver> driver) {
        this.driver = driver;
    }

    
    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        if (context.getExecutionException().isEmpty() || driver.get() == null) {
            return;
        }

        var name = context.getRequiredTestClass().getSimpleName() + "_"
                + context.getRequiredTestMethod().getName() + "_"
                + System.currentTimeMillis();
        ScreenshotSupport.capture(driver.get(), name);
    }
}