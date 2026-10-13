package pages;

import config.TestConfig;
import data.TestUser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


public class LoginPage {

    
    private final WebDriver driver;

    
    private final WebDriverWait wait;

    
    private final By username = By.cssSelector("[data-test='username']");

    
    private final By password = By.cssSelector("[data-test='password']");

    
    private final By loginButton = By.cssSelector("[data-test='login-button']");

    
    private final By error = By.cssSelector("[data-test='error']");

    
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TestConfig.TIMEOUT);
    }

    
    public LoginPage open() {
        driver.get(TestConfig.BASE_URL);
        return this;
    }

    
    public void loginAs(TestUser user) {
        login(user.username(), user.password());
    }

    
    public void login(String user, String pass) {
        if (user != null) {
            driver.findElement(username).sendKeys(user);
        }
        if (pass != null) {
            driver.findElement(password).sendKeys(pass);
        }
        driver.findElement(loginButton).click();
    }

    public LoginPage waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(loginButton));
        return this;
    }

    public String errorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(error)).getText();
    }

    public boolean isErrorMessageDisplayed() {
        return driver.findElements(error).stream().anyMatch(element -> element.isDisplayed());
    }

    
    public boolean isLoginButtonDisplayed() {
        return driver.findElement(loginButton).isDisplayed();
    }

    
    public String passwordFieldType() {
        return driver.findElement(password).getAttribute("type");
    }

    
    public String currentUrl() {
        return driver.getCurrentUrl();
    }
}