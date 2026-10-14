package pages;

import config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


public class CheckoutStepOnePage {

    private final WebDriver driver;

    private final WebDriverWait wait;

    
    private final By title = By.cssSelector("[data-test='title']");

    
    private final By checkoutInfoContainer =
            By.cssSelector("[data-test='checkout-info-container']");

    
    private final By firstNameInput = By.cssSelector("[data-test='firstName']");

    
    private final By lastNameInput = By.cssSelector("[data-test='lastName']");

    
    private final By postalCodeInput = By.cssSelector("[data-test='postalCode']");

    
    private final By errorMessage = By.cssSelector(".error-message-container");

    
    private final By cartBadge = By.cssSelector("[data-test='shopping-cart-badge']");

    
    private final By cancelButton = By.cssSelector("[data-test='cancel']");

    
    private final By continueButton = By.cssSelector("[data-test='continue']");

    
    public CheckoutStepOnePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TestConfig.TIMEOUT);
    }

    
    public CheckoutStepOnePage waitUntilLoaded() {
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(checkoutInfoContainer));
        wait.until(ExpectedConditions.visibilityOfElementLocated(firstNameInput));
        return this;
    }

    
    public String title() {
        return driver.findElement(title).getText();
    }

    
    public void enterFirstName(String firstName) {
        var input = driver.findElement(firstNameInput);
        input.clear();
        input.sendKeys(firstName);
    }

    
    public void enterLastName(String lastName) {
        var input = driver.findElement(lastNameInput);
        input.clear();
        input.sendKeys(lastName);
    }

    
    public void enterPostalCode(String postalCode) {
        var input = driver.findElement(postalCodeInput);
        input.clear();
        input.sendKeys(postalCode);
    }

    
    public void submitCustomerInformation() {
        wait.until(ExpectedConditions.elementToBeClickable(continueButton)).click();
    }

    
    public String firstNameAccessibleName() {
        return driver.findElement(firstNameInput).getAccessibleName();
    }

    
    public String lastNameAccessibleName() {
        return driver.findElement(lastNameInput).getAccessibleName();
    }

    
    public String postalCodeAccessibleName() {
        return driver.findElement(postalCodeInput).getAccessibleName();
    }

    
    public void tabForwardFromFirstName() {
        driver.findElement(firstNameInput).sendKeys(Keys.TAB);
    }

    
    public void tabForwardFromLastName() {
        driver.findElement(lastNameInput).sendKeys(Keys.TAB);
    }

    
    public String focusedElementAccessibleName() {
        return driver.switchTo().activeElement().getAccessibleName();
    }

    
    public boolean isFocusedElementFocusVisible() {
        return Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(
                "return arguments[0].matches(':focus-visible');",
                driver.switchTo().activeElement()));
    }

    
    public int cartBadgeCount() {
        var badges = driver.findElements(cartBadge);
        return badges.isEmpty() ? 0 : Integer.parseInt(badges.get(0).getText());
    }

    
    public String errorMessage() {
        return wait.until(driver -> {
            var message = driver.findElement(errorMessage).getText();
            return message.isBlank() ? null : message;
        });
    }

    
    public CartPage cancel() {
        wait.until(ExpectedConditions.elementToBeClickable(cancelButton)).click();
        return new CartPage(driver).waitUntilLoaded();
    }

    
    public CheckoutStepTwoPage continueCheckout() {
        submitCustomerInformation();
        return new CheckoutStepTwoPage(driver).waitUntilLoaded();
    }
}
