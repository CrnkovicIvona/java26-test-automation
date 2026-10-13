package pages;

import config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class CartPage {

    private final WebDriver driver;

    private final WebDriverWait wait;

    private final By title = By.cssSelector("[data-test='title']");

    private final By cartContents = By.cssSelector("[data-test='cart-contents-container']");

    private final By cartList = By.cssSelector("[data-test='cart-list']");

    private final By cartItems = By.cssSelector("[data-test='inventory-item']");

    private final By itemQuantity = By.cssSelector("[data-test='item-quantity']");

    private final By itemName = By.cssSelector("[data-test='inventory-item-name']");

    private final By itemDescription = By.cssSelector("[data-test='inventory-item-desc']");

    private final By itemPrice = By.cssSelector("[data-test='inventory-item-price']");

    private final By removeButton = By.cssSelector("[data-test^='remove-']");

    private final By cartBadge = By.cssSelector("[data-test='shopping-cart-badge']");

    private final By continueShoppingButton = By.cssSelector("[data-test='continue-shopping']");

    private final By checkoutButton = By.cssSelector("[data-test='checkout']");

   
    public CartPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TestConfig.TIMEOUT);
    }

    public CartPage waitUntilLoaded() {
        wait.until(ExpectedConditions.urlContains("cart.html"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(cartContents));
        wait.until(ExpectedConditions.visibilityOfElementLocated(cartList));
        return this;
    }

    public String title() {
        return driver.findElement(title).getText();
    }

    public List<CartItem> items() {
        return driver.findElements(cartItems).stream()
                .map(this::readCartItem)
                .toList();
    }
    
    public int itemCount() {
        return driver.findElements(cartItems).size();
    }

    public int badgeCount() {
        var badges = driver.findElements(cartBadge);
        return badges.isEmpty() ? 0 : Integer.parseInt(badges.get(0).getText());
    }

    public boolean isEmpty() {
        return driver.findElements(cartItems).isEmpty();
    }

    public void removeProduct(String name) {
        var row = findCartItem(name);
        wait.until(ExpectedConditions.elementToBeClickable(
                row.findElement(removeButton))).click();
        wait.until(driver -> driver.findElements(cartItems).stream()
                .noneMatch(item -> item.findElement(itemName).getText().equals(name)));
    }

    public InventoryPage continueShopping() {
        wait.until(ExpectedConditions.elementToBeClickable(continueShoppingButton)).click();
        return new InventoryPage(driver).waitUntilLoaded();
    }

    public CheckoutStepOnePage proceedToCheckout() {
        wait.until(ExpectedConditions.elementToBeClickable(checkoutButton)).click();
        return new CheckoutStepOnePage(driver).waitUntilLoaded();
    }

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

    private WebElement findCartItem(String name) {
        return driver.findElements(cartItems).stream()
                .filter(item -> item.findElement(itemName).getText().equals(name))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "No cart item found with name: " + name));
    }
    
    public record CartItem(
            int quantity,
            String name,
            String description,
            String price,
            String removeButtonLabel,
            boolean removeButtonEnabled) {}
}
