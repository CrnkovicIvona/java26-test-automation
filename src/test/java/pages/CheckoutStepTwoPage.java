package pages;

import config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/**
 * Page Object Model representation of SauceDemo's checkout overview page.
 *
 * <p>It exposes the order summary, payment and shipping information, totals,
 * cart badge, and the available Cancel and Finish actions.
 */
public class CheckoutStepTwoPage {

    /** Browser session used to locate and interact with page elements. */
    private final WebDriver driver;

    /** Explicit wait used to synchronize overview loading and button actions. */
    private final WebDriverWait wait;

    /** Stable application test attribute identifying the page title. */
    private final By title = By.cssSelector("[data-test='title']");

    /** Stable application test attribute identifying the checkout summary. */
    private final By summaryContainer =
            By.cssSelector("[data-test='checkout-summary-container']");

    /** Stable application test attribute identifying the summary item list. */
    private final By summaryList = By.cssSelector("[data-test='cart-list']");

    /** Stable application test attribute identifying each summarized product row. */
    private final By summaryItems = By.cssSelector("[data-test='inventory-item']");

    /** Stable application test attribute identifying a summarized item's quantity. */
    private final By itemQuantity = By.cssSelector("[data-test='item-quantity']");

    /** Stable application test attribute identifying a summarized item's name. */
    private final By itemName = By.cssSelector("[data-test='inventory-item-name']");

    /** Stable application test attribute identifying a summarized item's description. */
    private final By itemDescription = By.cssSelector("[data-test='inventory-item-desc']");

    /** Stable application test attribute identifying a summarized item's price. */
    private final By itemPrice = By.cssSelector("[data-test='inventory-item-price']");

    /** Stable application test attribute identifying the payment information value. */
    private final By paymentInformation =
            By.cssSelector("[data-test='payment-info-value']");

    /** Stable application test attribute identifying the shipping information value. */
    private final By shippingInformation =
            By.cssSelector("[data-test='shipping-info-value']");

    /** Stable application test attribute identifying the item subtotal text. */
    private final By subtotal = By.cssSelector("[data-test='subtotal-label']");

    /** Stable application test attribute identifying the tax text. */
    private final By tax = By.cssSelector("[data-test='tax-label']");

    /** Stable application test attribute identifying the final total text. */
    private final By total = By.cssSelector("[data-test='total-label']");

    /** Stable application test attribute identifying the cart item-count badge. */
    private final By cartBadge = By.cssSelector("[data-test='shopping-cart-badge']");

    /** Stable application test attribute identifying the cancel button. */
    private final By cancelButton = By.cssSelector("[data-test='cancel']");

    /** Stable application test attribute identifying the finish button. */
    private final By finishButton = By.cssSelector("[data-test='finish']");

    /**
     * Creates the page object and its explicit wait for the supplied browser.
     *
     * @param driver active browser session controlled by the current test
     */
    public CheckoutStepTwoPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TestConfig.TIMEOUT);
    }

    /**
     * Waits until the browser reaches checkout overview and its summary is visible.
     *
     * @return this page object to allow fluent wait-and-read calls
     */
    public CheckoutStepTwoPage waitUntilLoaded() {
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(summaryContainer));
        wait.until(ExpectedConditions.visibilityOfElementLocated(summaryList));
        return this;
    }

    /**
     * Reads the visible checkout overview title.
     *
     * @return displayed text of the page title
     */
    public String title() {
        return driver.findElement(title).getText();
    }

    /**
     * Reads the displayed product rows in the checkout summary.
     *
     * @return immutable snapshots of the summarized products; an empty list when there are none
     */
    public List<CheckoutItem> items() {
        return driver.findElements(summaryItems).stream()
                .map(this::readCheckoutItem)
                .toList();
    }

    /**
     * Returns the number of product rows displayed in the checkout summary.
     *
     * @return number of summarized product rows, or zero when the summary is empty
     */
    public int itemCount() {
        return driver.findElements(summaryItems).size();
    }

    /**
     * Reads the cart badge count in the checkout page header.
     *
     * @return numeric count shown in the badge, or zero when the badge is absent
     */
    public int cartBadgeCount() {
        var badges = driver.findElements(cartBadge);
        return badges.isEmpty() ? 0 : Integer.parseInt(badges.get(0).getText());
    }

    /**
     * Reads the payment information value displayed in the order summary.
     *
     * @return visible payment information
     */
    public String paymentInformation() {
        return driver.findElement(paymentInformation).getText();
    }

    /**
     * Reads the shipping information value displayed in the order summary.
     *
     * @return visible shipping information
     */
    public String shippingInformation() {
        return driver.findElement(shippingInformation).getText();
    }

    /**
     * Reads the item subtotal exactly as displayed by the application.
     *
     * @return visible item subtotal text
     */
    public String subtotal() {
        return driver.findElement(subtotal).getText();
    }

    /**
     * Reads the tax exactly as displayed by the application.
     *
     * @return visible tax text
     */
    public String tax() {
        return driver.findElement(tax).getText();
    }

    /**
     * Reads the final total exactly as displayed by the application.
     *
     * @return visible final total text
     */
    public String total() {
        return driver.findElement(total).getText();
    }

    /**
     * Cancels the overview and waits for the product catalog to load.
     *
     * @return loaded product catalog page object
     */
    public InventoryPage cancel() {
        wait.until(ExpectedConditions.elementToBeClickable(cancelButton)).click();
        return new InventoryPage(driver).waitUntilLoaded();
    }

    /**
     * Confirms the order and waits for the completed checkout page.
     *
     * @return loaded checkout-complete page object
     */
    public CheckoutCompletePage finishCheckout() {
        wait.until(ExpectedConditions.elementToBeClickable(finishButton)).click();
        return new CheckoutCompletePage(driver).waitUntilLoaded();
    }

    /**
     * Extracts the visible product information from one order-summary row.
     *
     * @param row summary row whose product details are read
     * @return immutable snapshot of the summarized product
     */
    private CheckoutItem readCheckoutItem(WebElement row) {
        return new CheckoutItem(
                Integer.parseInt(row.findElement(itemQuantity).getText()),
                row.findElement(itemName).getText(),
                row.findElement(itemDescription).getText(),
                row.findElement(itemPrice).getText());
    }

    /**
     * Immutable snapshot of the product information shown in the checkout summary.
     *
     * @param quantity visible product quantity
     * @param name visible product name
     * @param description visible product description
     * @param price visible product price text
     */
    public record CheckoutItem(int quantity, String name, String description, String price) {}
}
