package pages;

import config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;


public class CheckoutStepTwoPage {

    
    private final WebDriver driver;

    
    private final WebDriverWait wait;

    
    private final By title = By.cssSelector("[data-test='title']");

    
    private final By summaryContainer =
            By.cssSelector("[data-test='checkout-summary-container']");

    
    private final By summaryList = By.cssSelector("[data-test='cart-list']");

    
    private final By summaryItems = By.cssSelector("[data-test='inventory-item']");

    
    private final By itemQuantity = By.cssSelector("[data-test='item-quantity']");

    
    private final By itemName = By.cssSelector("[data-test='inventory-item-name']");

    
    private final By itemDescription = By.cssSelector("[data-test='inventory-item-desc']");

    
    private final By itemPrice = By.cssSelector("[data-test='inventory-item-price']");

    
    private final By paymentInformation =
            By.cssSelector("[data-test='payment-info-value']");

    
    private final By shippingInformation =
            By.cssSelector("[data-test='shipping-info-value']");

    
    private final By subtotal = By.cssSelector("[data-test='subtotal-label']");

    
    private final By tax = By.cssSelector("[data-test='tax-label']");

    
    private final By total = By.cssSelector("[data-test='total-label']");

    
    private final By cartBadge = By.cssSelector("[data-test='shopping-cart-badge']");

    
    private final By cancelButton = By.cssSelector("[data-test='cancel']");

    
    private final By finishButton = By.cssSelector("[data-test='finish']");

    
    public CheckoutStepTwoPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TestConfig.TIMEOUT);
    }

    
    public CheckoutStepTwoPage waitUntilLoaded() {
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(summaryContainer));
        wait.until(ExpectedConditions.visibilityOfElementLocated(summaryList));
        return this;
    }

    
    public String title() {
        return driver.findElement(title).getText();
    }

    
    public List<CheckoutItem> items() {
        return driver.findElements(summaryItems).stream()
                .map(this::readCheckoutItem)
                .toList();
    }

    
    public int itemCount() {
        return driver.findElements(summaryItems).size();
    }

    
    public int cartBadgeCount() {
        var badges = driver.findElements(cartBadge);
        return badges.isEmpty() ? 0 : Integer.parseInt(badges.get(0).getText());
    }

    
    public String paymentInformation() {
        return driver.findElement(paymentInformation).getText();
    }

    
    public String shippingInformation() {
        return driver.findElement(shippingInformation).getText();
    }

    
    public String subtotal() {
        return driver.findElement(subtotal).getText();
    }

    
    public String tax() {
        return driver.findElement(tax).getText();
    }

    
    public String total() {
        return driver.findElement(total).getText();
    }

    
    public InventoryPage cancel() {
        wait.until(ExpectedConditions.elementToBeClickable(cancelButton)).click();
        return new InventoryPage(driver).waitUntilLoaded();
    }

    
    public CheckoutCompletePage finishCheckout() {
        wait.until(ExpectedConditions.elementToBeClickable(finishButton)).click();
        return new CheckoutCompletePage(driver).waitUntilLoaded();
    }

    
    private CheckoutItem readCheckoutItem(WebElement row) {
        return new CheckoutItem(
                Integer.parseInt(row.findElement(itemQuantity).getText()),
                row.findElement(itemName).getText(),
                row.findElement(itemDescription).getText(),
                row.findElement(itemPrice).getText());
    }

    
    public record CheckoutItem(int quantity, String name, String description, String price) {}
}
