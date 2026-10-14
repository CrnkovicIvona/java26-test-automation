package pages;

import config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


public class ProductDetailsPage {

    
    private final WebDriver driver;

    
    private final WebDriverWait wait;

    
    private final By detailsContainer = By.cssSelector("[data-test='inventory-container']");

    
    private final By productName = By.cssSelector("[data-test='inventory-item-name']");

    
    private final By productDescription = By.cssSelector("[data-test='inventory-item-desc']");

    
    private final By productPrice = By.cssSelector("[data-test='inventory-item-price']");

    
    private final By productImage = By.cssSelector("[data-test^='item-'][data-test$='-img']");

    
    private final By productCartButton = By.cssSelector(
            "[data-test='add-to-cart'], [data-test='remove']");

    
    private final By backToProductsButton = By.cssSelector("[data-test='back-to-products']");

    
    public ProductDetailsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TestConfig.TIMEOUT);
    }

    
    public ProductDetailsPage waitUntilLoaded() {
        wait.until(ExpectedConditions.urlContains("inventory-item.html"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(detailsContainer));
        wait.until(ExpectedConditions.visibilityOfElementLocated(productName));
        return this;
    }

    
    public ProductDetails details() {
        var image = driver.findElement(productImage);
        var imageLoaded = Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(
                "return arguments[0].complete && arguments[0].naturalWidth > 0;",
                image));
        var cartButton = driver.findElement(productCartButton);

        return new ProductDetails(
                driver.findElement(productName).getText(),
                driver.findElement(productDescription).getText(),
                driver.findElement(productPrice).getText(),
                image.getAttribute("alt"),
                image.isDisplayed(),
                imageLoaded,
                cartButton.getText(),
                cartButton.isEnabled());
    }

    
    public InventoryPage backToProducts() {
        wait.until(ExpectedConditions.elementToBeClickable(backToProductsButton)).click();
        return new InventoryPage(driver).waitUntilLoaded();
    }

    
    public record ProductDetails(
            String name,
            String description,
            String price,
            String imageAlt,
            boolean imageDisplayed,
            boolean imageLoaded,
            String cartButtonLabel,
            boolean cartButtonEnabled) {}
}
