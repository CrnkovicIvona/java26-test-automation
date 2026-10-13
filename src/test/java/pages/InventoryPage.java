package pages;

import config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;


public class InventoryPage {

    
    private final WebDriver driver;

    
    private final WebDriverWait wait;

    
    private final By title = By.cssSelector("[data-test='title']");

    
    private final By inventoryList = By.cssSelector("[data-test='inventory-list']");

    
    private final By productCards = By.cssSelector("[data-test='inventory-item']");

    
    private final By productImage = By.cssSelector("[data-test^='inventory-item-'][data-test$='-img']");

    
    private final By productName = By.cssSelector("[data-test='inventory-item-name']");

    
    private final By productDescription = By.cssSelector("[data-test='inventory-item-desc']");

    
    private final By productPrice = By.cssSelector("[data-test='inventory-item-price']");

    
    private final By productCartButton = By.cssSelector(
            "[data-test^='add-to-cart-'], [data-test^='remove-']");

    
    private final By shoppingCart = By.cssSelector("[data-test='shopping-cart-link']");

    
    private final By shoppingCartBadge = By.cssSelector("[data-test='shopping-cart-badge']");

    
    private final By sortContainer = By.cssSelector("[data-test='product-sort-container']");

    
    private final By activeSortOption = By.cssSelector("[data-test='active-option']");

    
    private final By menuButton = By.id("react-burger-menu-btn");

    
    private final By logoutLink = By.cssSelector("[data-test='logout-sidebar-link']");

    
    public InventoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TestConfig.TIMEOUT);
    }

    
    public InventoryPage waitUntilLoaded() {
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryList));
        return this;
    }

    
    public String title() {
        return driver.findElement(title).getText();
    }

    
    public ProductDetailsPage openProductDetails(String name) {
        var nameLink = wait.until(ExpectedConditions.elementToBeClickable(
                findProductCard(name).findElement(productName)));
        nameLink.click();
        return new ProductDetailsPage(driver).waitUntilLoaded();
    }

    
    public List<ProductDetails> productDetails() {
        return driver.findElements(productCards).stream()
                .map(this::readProductDetails)
                .toList();
    }

    
    public void sortProducts(String optionValue) {
        var dropdown = new Select(wait.until(
                ExpectedConditions.elementToBeClickable(sortContainer)));
        dropdown.selectByValue(optionValue);

        wait.until(driver -> {
            var selectedLabel = new Select(driver.findElement(sortContainer))
                    .getFirstSelectedOption().getText();
            var visibleLabel = driver.findElement(activeSortOption).getText();
            return selectedLabel.equals(visibleLabel);
        });
    }

    
    public String activeSortOption() {
        return driver.findElement(activeSortOption).getText();
    }

    
    public void addProductToCart(String name) {
        var expectedCartCount = shoppingCartItemCount() + 1;
        var card = findProductCard(name);
        wait.until(ExpectedConditions.elementToBeClickable(
                card.findElement(productCartButton))).click();
        wait.until(driver -> shoppingCartItemCount() == expectedCartCount);
    }

    
    public CartPage openCart() {
        wait.until(ExpectedConditions.elementToBeClickable(shoppingCart)).click();
        return new CartPage(driver).waitUntilLoaded();
    }

    
    public int shoppingCartItemCount() {
        var badges = driver.findElements(shoppingCartBadge);
        return badges.isEmpty() ? 0 : Integer.parseInt(badges.get(0).getText());
    }

    
    public String productCartButtonLabel(String name) {
        return findProductCard(name).findElement(productCartButton).getText();
    }

    
    private ProductDetails readProductDetails(WebElement card) {
        var image = card.findElement(productImage);
        var imageLoaded = Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(
                "return arguments[0].complete && arguments[0].naturalWidth > 0;",
                image));
        var name = card.findElement(productName).getText();
        var description = card.findElement(productDescription).getText();
        var price = card.findElement(productPrice).getText();
        var cartButton = card.findElement(productCartButton);

        return new ProductDetails(
                name,
                description,
                price,
                image.getAttribute("alt"),
                image.isDisplayed(),
                imageLoaded,
                cartButton.getText(),
                cartButton.isEnabled());
    }

    
    private WebElement findProductCard(String name) {
        return driver.findElements(productCards).stream()
                .filter(card -> card.findElement(productName).getText().equals(name))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "No inventory product found with name: " + name));
    }

    
    public void logout() {
        driver.findElement(menuButton).click();
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();
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