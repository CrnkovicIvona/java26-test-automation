package pages;

import config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


public class CheckoutCompletePage {

    private final WebDriver driver;

    private final WebDriverWait wait;

    private final By title = By.cssSelector("[data-test='title']");

    private final By completeContainer =
            By.cssSelector("[data-test='checkout-complete-container']");

    private final By confirmationHeading =
            By.cssSelector("[data-test='complete-header']");

    private final By confirmationMessage =
            By.cssSelector("[data-test='complete-text']");

    private final By deliveryImage = By.cssSelector("[data-test='pony-express']");

    private final By cartBadge = By.cssSelector("[data-test='shopping-cart-badge']");

    private final By backToProductsButton =
            By.cssSelector("[data-test='back-to-products']");

    public CheckoutCompletePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TestConfig.TIMEOUT);
    }

   
    public CheckoutCompletePage waitUntilLoaded() {
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(completeContainer));
        wait.until(ExpectedConditions.visibilityOfElementLocated(confirmationHeading));
        wait.until(ExpectedConditions.visibilityOfElementLocated(deliveryImage));
        return this;
    }

    
    public String title() {
        return driver.findElement(title).getText();
    }

    
    public String confirmationHeading() {
        return driver.findElement(confirmationHeading).getText();
    }

   
    public String confirmationMessage() {
        return driver.findElement(confirmationMessage).getText();
    }

    
    public String deliveryImageAltText() {
        return driver.findElement(deliveryImage).getAttribute("alt");
    }

    
    public boolean isDeliveryImageDisplayed() {
        return driver.findElement(deliveryImage).isDisplayed();
    }

   
    public int cartBadgeCount() {
        var badges = driver.findElements(cartBadge);
        return badges.isEmpty() ? 0 : Integer.parseInt(badges.get(0).getText());
    }

  
    public InventoryPage backToProducts() {
        wait.until(ExpectedConditions.elementToBeClickable(backToProductsButton)).click();
        return new InventoryPage(driver).waitUntilLoaded();
    }
}
