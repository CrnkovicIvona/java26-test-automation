package pages;

import config.TestConfig;
import data.TestUser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object Model representation of the SauceDemo login page.
 *
 * <p>This class keeps login-page locators and interactions in one place so
 * test classes can focus on behavior and assertions.
 */
public class LoginPage {

    /** Browser session used to locate and interact with page elements. */
    private final WebDriver driver;

    /** Explicit wait used when login feedback must become visible. */
    private final WebDriverWait wait;

    /** Stable application test attribute identifying the username input. */
    private final By username = By.cssSelector("[data-test='username']");

    /** Stable application test attribute identifying the password input. */
    private final By password = By.cssSelector("[data-test='password']");

    /** Stable application test attribute identifying the login button. */
    private final By loginButton = By.cssSelector("[data-test='login-button']");

    /** Stable application test attribute identifying the login error message. */
    private final By error = By.cssSelector("[data-test='error']");

    /**
     * Creates the page object and its explicit wait for the supplied browser.
     *
     * @param driver active browser session controlled by the current test
     */
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TestConfig.TIMEOUT);
    }

    /**
     * Navigates the browser to the application's login page.
     *
     * @return this page object to allow fluent setup calls
     */
    public LoginPage open() {
        driver.get(TestConfig.BASE_URL);
        return this;
    }

    /**
     * Enters the supplied account's credentials and submits the login form.
     *
     * @param user account whose credentials should be submitted
     */
    public void loginAs(TestUser user) {
        login(user.username(), user.password());
    }

    /**
     * Enters whichever credentials are non-null and submits the form.
     *
     * <p>A {@code null} username or password is deliberately left blank so
     * validation scenarios can verify required-field behavior.
     *
     * @param user username to enter, or {@code null} to leave the field empty
     * @param pass password to enter, or {@code null} to leave the field empty
     */
    public void login(String user, String pass) {
        if (user != null) {
            driver.findElement(username).sendKeys(user);
        }
        if (pass != null) {
            driver.findElement(password).sendKeys(pass);
        }
        driver.findElement(loginButton).click();
    }

    /**
     * Waits until the login error is visible and returns its displayed text.
     *
     * @return text of the currently visible login error
     */
    public String errorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(error)).getText();
    }

    /**
     * Checks whether the login button is currently displayed.
     *
     * @return {@code true} if the login button is visible; otherwise {@code false}
     */
    public boolean isLoginButtonDisplayed() {
        return driver.findElement(loginButton).isDisplayed();
    }

    /**
     * Reads the HTML input type of the password field.
     *
     * @return value of the field's {@code type} attribute
     */
    public String passwordFieldType() {
        return driver.findElement(password).getAttribute("type");
    }

    /**
     * Returns the browser's current address for navigation assertions.
     *
     * @return current page URL
     */
    public String currentUrl() {
        return driver.getCurrentUrl();
    }
}