package pages;

import config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object Model representation of the first SauceDemo checkout step.
 *
 * <p>This page collects customer information before the checkout overview.
 */
public class CheckoutStepOnePage {

    /** Browser session used to locate and interact with page elements. */
    private final WebDriver driver;

    /** Explicit wait used to synchronize page loading and validation feedback. */
    private final WebDriverWait wait;

    /** Stable application test attribute identifying the page title. */
    private final By title = By.cssSelector("[data-test='title']");

    /** Stable application test attribute identifying the checkout form container. */
    private final By checkoutInfoContainer =
            By.cssSelector("[data-test='checkout-info-container']");

    /** Stable application test attribute identifying the first-name input. */
    private final By firstNameInput = By.cssSelector("[data-test='firstName']");

    /** Stable application test attribute identifying the last-name input. */
    private final By lastNameInput = By.cssSelector("[data-test='lastName']");

    /** Stable application test attribute identifying the postal-code input. */
    private final By postalCodeInput = By.cssSelector("[data-test='postalCode']");

    /** Container that displays validation feedback after an invalid submission. */
    private final By errorMessage = By.cssSelector(".error-message-container");

    /** Stable application test attribute identifying the cart item-count badge. */
    private final By cartBadge = By.cssSelector("[data-test='shopping-cart-badge']");

    /** Stable application test attribute identifying the cancel button. */
    private final By cancelButton = By.cssSelector("[data-test='cancel']");

    /** Stable application test attribute identifying the continue button. */
    private final By continueButton = By.cssSelector("[data-test='continue']");

    /**
     * Creates the page object and its explicit wait for the supplied browser.
     *
     * @param driver active browser session controlled by the current test
     */
    public CheckoutStepOnePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TestConfig.TIMEOUT);
    }

    /**
     * Waits until the browser reaches the first checkout step and its form is visible.
     *
     * @return this page object to allow fluent wait-and-read calls
     */
    public CheckoutStepOnePage waitUntilLoaded() {
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(checkoutInfoContainer));
        wait.until(ExpectedConditions.visibilityOfElementLocated(firstNameInput));
        return this;
    }

    /**
     * Reads the visible checkout page title.
     *
     * @return displayed text of the checkout page title
     */
    public String title() {
        return driver.findElement(title).getText();
    }

    /**
     * Enters the customer's first name, replacing any existing value.
     *
     * @param firstName first name to enter
     */
    public void enterFirstName(String firstName) {
        var input = driver.findElement(firstNameInput);
        input.clear();
        input.sendKeys(firstName);
    }

    /**
     * Enters the customer's last name, replacing any existing value.
     *
     * @param lastName last name to enter
     */
    public void enterLastName(String lastName) {
        var input = driver.findElement(lastNameInput);
        input.clear();
        input.sendKeys(lastName);
    }

    /**
     * Enters the customer's postal code, replacing any existing value.
     *
     * @param postalCode postal or ZIP code to enter
     */
    public void enterPostalCode(String postalCode) {
        var input = driver.findElement(postalCodeInput);
        input.clear();
        input.sendKeys(postalCode);
    }

    /**
     * Submits the customer information form without assuming it is valid.
     *
     * <p>This action is intended for validation tests. Use
     * {@link #continueCheckout()} when valid data should advance to the overview.
     */
    public void submitCustomerInformation() {
        wait.until(ExpectedConditions.elementToBeClickable(continueButton)).click();
    }

    /**
     * Reads the accessible name computed for the first-name input.
     *
     * @return accessible name exposed to assistive technology
     */
    public String firstNameAccessibleName() {
        return driver.findElement(firstNameInput).getAccessibleName();
    }

    /**
     * Reads the accessible name computed for the last-name input.
     *
     * @return accessible name exposed to assistive technology
     */
    public String lastNameAccessibleName() {
        return driver.findElement(lastNameInput).getAccessibleName();
    }

    /**
     * Reads the accessible name computed for the postal-code input.
     *
     * @return accessible name exposed to assistive technology
     */
    public String postalCodeAccessibleName() {
        return driver.findElement(postalCodeInput).getAccessibleName();
    }

    /**
     * Moves keyboard focus forward from the first-name input.
     */
    public void tabForwardFromFirstName() {
        driver.findElement(firstNameInput).sendKeys(Keys.TAB);
    }

    /**
     * Moves keyboard focus forward from the last-name input.
     */
    public void tabForwardFromLastName() {
        driver.findElement(lastNameInput).sendKeys(Keys.TAB);
    }

    /**
     * Reads the accessible name of the currently focused element.
     *
     * @return accessible name of the active element
     */
    public String focusedElementAccessibleName() {
        return driver.switchTo().activeElement().getAccessibleName();
    }

    /**
     * Checks whether keyboard focus is visibly indicated on the active element.
     *
     * @return {@code true} when the browser considers the active element focus-visible
     */
    public boolean isFocusedElementFocusVisible() {
        return Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(
                "return arguments[0].matches(':focus-visible');",
                driver.switchTo().activeElement()));
    }

    /**
     * Reads the cart badge count shown in the checkout header.
     *
     * @return number displayed in the badge, or zero when the badge is absent
     */
    public int cartBadgeCount() {
        var badges = driver.findElements(cartBadge);
        return badges.isEmpty() ? 0 : Integer.parseInt(badges.get(0).getText());
    }

    /**
     * Waits for validation feedback and returns its displayed text.
     *
     * @return text of the currently displayed checkout validation error
     */
    public String errorMessage() {
        return wait.until(driver -> {
            var message = driver.findElement(errorMessage).getText();
            return message.isBlank() ? null : message;
        });
    }

    /**
     * Cancels checkout and waits for the shopping cart page to load.
     *
     * @return loaded shopping cart page object
     */
    public CartPage cancel() {
        wait.until(ExpectedConditions.elementToBeClickable(cancelButton)).click();
        return new CartPage(driver).waitUntilLoaded();
    }

    /**
     * Submits the customer information form and waits for the checkout overview.
     *
     * <p>If the supplied values are invalid or incomplete, the application
     * displays validation feedback and this method's wait will time out. Use
     * this navigation method for valid data; use {@link #errorMessage()} for
     * validation scenarios.
     *
     * @return loaded checkout overview page object
     */
    public CheckoutStepTwoPage continueCheckout() {
        submitCustomerInformation();
        return new CheckoutStepTwoPage(driver).waitUntilLoaded();
    }
}
