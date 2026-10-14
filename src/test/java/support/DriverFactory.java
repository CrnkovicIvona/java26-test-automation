package support;

import config.TestConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Map;


public final class DriverFactory {

    
    public static WebDriver create() {
        var options = new ChromeOptions();
        
        options.addArguments("--window-size=1920,1080");
        if (TestConfig.HEADLESS) {
            
            options.addArguments("--headless=new");
        }
        
        options.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false));
        return new ChromeDriver(options);
    }

    
    private DriverFactory() {}
}