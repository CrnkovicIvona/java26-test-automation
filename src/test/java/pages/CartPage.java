package pages;

import config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/**
 * Page Object Model representation of the SauceDemo shopping cart.
 *
 * <p>It exposes the cart's visible contents and the actions available on both
 * the empty and populated cart pages.
 */
public class CartPage {

    /** Browser session used to locate and interact with cart elements. */
    private final WebDriver driver;

    /** Explicit wait used to synchronize cart loading and user actions. */
    private final WebDriverWait wait;

    /** Stable application test attribute identifying the page title. */
    private final By title = By.cssSelector("[data-test='title']");

    /** Stable application test attribute identifying the main cart container. */
    private final By cartContents = By.cssSelector("[data-test='cart-contents-container']");

    /** Stable application test attribute identifying the list of cart items. */
    private final By cartList = By.cssSelector("[data-test='cart-list']");

    /** Stable application test attribute identifying each cart product row. */
    private final By cartItems = By.cssSelector("[data-test='inventory-item']");

    /** Stable application test attribute identifying the quantity of a cart item. */
    private final By itemQuantity = By.cssSelector("[data-test='item-quantity']");

    /** Stable application test attribute identifying a cart item's product name. */
    private final By itemName = By.cssSelector("[data-test='inventory-item-name']");

    /** Stable application test attribute identifying a cart item's description. */
    private final By itemDescription = By.cssSelector("[data-test='inventory-item-desc']");

    /** Stable application test attribute identifying a cart item's price. */
    private final By itemPrice = By.cssSelector("[data-test='inventory-item-price']");

    /** Identifies the remove button inside a cart item row. */
    private final By removeButton = By.cssSelector("[data-test^='remove-']");

    /** Stable application test attribute identifying the cart item-count badge. */
    private final By cartBadge = By.cssSelector("[data-test='shopping-cart-badge']");

    /** Stable application test attribute identifying the continue-shopping button. */
    private final By continueShoppingButton = By.cssSelector("[data-test='continue-shopping']");

    /** Stable application test attribute identifying the checkout button. */
    private final By checkoutButton = By.cssSelector("[data-test='checkout']");

    /**
     * Creates the page object and its explicit wait for the supplied browser.
     *
     * @param driver active browser session controlled by the current test
     */
    public CartPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TestConfig.TIMEOUT);
    }

    /**
     * Waits until the browser reaches the cart page and its contents are visible.
     *
     * @return this page object to allow fluent wait-and-read calls
     */
    public CartPage waitUntilLoaded() {
        wait.until(ExpectedConditions.urlContains("cart.html"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(cartContents));
        wait.until(ExpectedConditions.visibilityOfElementLocated(cartList));
        return this;
    }

    /**
     * Reads the visible cart page title.
     *
     * @return displayed text of the cart page title
     */
    public String title() {
        return driver.findElement(title).getText();
    }

    /**
     * Reads the visible details for every product row in the cart.
     *
     * @return immutable snapshots of the cart rows; an empty list when the cart is empty
     */
    public List<CartItem> items() {
        return driver.findElements(cartItems).stream()
                .map(this::readCartItem)
                .toList();
    }

    /**
     * Returns the number of product rows currently displayed in the cart.
     *
     * @return number of cart rows, or zero for an empty cart
     */
    public int itemCount() {
        return driver.findElements(cartItems).size();
    }

    /**
     * Reads the cart badge count, accounting for the badge being absent when empty.
     *
     * @return numeric item count shown in the header badge, or zero if no badge exists
     */
    public int badgeCount() {
        var badges = driver.findElements(cartBadge);
        return badges.isEmpty() ? 0 : Integer.parseInt(badges.get(0).getText());
    }

    /**
     * Determines whether the cart currently contains no product rows.
     *
     * @return {@code true} when the cart has no products; otherwise {@code false}
     */
    public boolean isEmpty() {
        return driver.findElements(cartItems).isEmpty();
    }

    /**
     * Removes the named product from the cart and waits for its row to disappear.
     *
     * @param name exact visible name of the product to remove
     * @throws NoSuchElementException if the named product is not in the cart
     */
    public void removeProduct(String name) {
        var row = findCartItem(name);
        wait.until(ExpectedConditions.elementToBeClickable(
                row.findElement(removeButton))).click();
        wait.until(driver -> driver.findElements(cartItems).stream()
                .noneMatch(item -> item.findElement(itemName).getText().equals(name)));
    }

    /**
     * Follows Continue Shopping and waits for the product catalog to load.
     *
     * @return loaded product catalog page object
     */
    public InventoryPage continueShopping() {
        wait.until(ExpectedConditions.elementToBeClickable(continueShoppingButton)).click();
        return new InventoryPage(driver).waitUntilLoaded();
    }

    /**
     * Follows Checkout and waits for the first checkout step to load.
     *
     * @return loaded first checkout step page object
     */
    public CheckoutStepOnePage proceedToCheckout() {
        wait.until(ExpectedConditions.elementToBeClickable(checkoutButton)).click();
        return new CheckoutStepOnePage(driver).waitUntilLoaded();
    }

    /**
     * Extracts the displayed information and remove-button state from a cart row.
     *
     * @param row cart row whose product details are read
     * @return snapshot of the cart row and its remove action
     */
    private CartItem readCartItem(WebElement row) {
        var remove = row.findElement(removeButton);
        return new CartItem(
                Integer.parseInt(row.findElement(itemQuantity).getText()),
                row.findElement(itemName).getText(),
                row.findElement(itemDescription).getText(),
                row.findElement(itemPrice).getText(),
                remove.getText(),
                remove.isEnabled());
    }

    /**
     * Finds the cart row with the exact visible product name.
     *
     * @param name exact visible product name to find
     * @return matching cart row
     * @throws NoSuchElementException if the cart has no product with that name
     */
    private WebElement findCartItem(String name) {
        return driver.findElements(cartItems).stream()
                .filter(item -> item.findElement(itemName).getText().equals(name))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "No cart item found with name: " + name));
    }

    /**
     * Immutable snapshot of the information and remove action shown for one cart item.
     *
     * @param quantity visible quantity for the cart item
     * @param name visible product name
     * @param description visible product description
     * @param price visible product price text
     * @param removeButtonLabel visible label of the remove button
     * @param removeButtonEnabled whether the remove button is enabled
     */
    public record CartItem(
            int quantity,
            String name,
            String description,
            String price,
            String removeButtonLabel,
            boolean removeButtonEnabled) {}
}
