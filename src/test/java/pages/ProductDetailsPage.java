package pages;

import config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object Model representation of one SauceDemo product details page.
 *
 * <p>It reads the selected product's displayed information and provides the
 * page's explicit navigation back to the product catalog.
 */
public class ProductDetailsPage {

    /** Browser session used to locate and interact with page elements. */
    private final WebDriver driver;

    /** Explicit wait used to synchronize page navigation and content. */
    private final WebDriverWait wait;

    /** Stable application test attribute identifying the details content. */
    private final By detailsContainer = By.cssSelector("[data-test='inventory-container']");

    /** Stable application test attribute identifying the product name. */
    private final By productName = By.cssSelector("[data-test='inventory-item-name']");

    /** Stable application test attribute identifying the product description. */
    private final By productDescription = By.cssSelector("[data-test='inventory-item-desc']");

    /** Stable application test attribute identifying the product price. */
    private final By productPrice = By.cssSelector("[data-test='inventory-item-price']");

    /** Stable application test attribute identifying the product image. */
    private final By productImage = By.cssSelector("[data-test^='item-'][data-test$='-img']");

    /** Product action button shown on the details page. */
    private final By productCartButton = By.cssSelector(
            "[data-test='add-to-cart'], [data-test='remove']");

    /** Stable application test attribute identifying the back-to-catalog button. */
    private final By backToProductsButton = By.cssSelector("[data-test='back-to-products']");

    /**
     * Creates the page object and its explicit wait for the supplied browser.
     *
     * @param driver active browser session controlled by the current test
     */
    public ProductDetailsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TestConfig.TIMEOUT);
    }

    /**
     * Waits until the product details route and its content are available.
     *
     * @return this page object to allow fluent wait-and-read calls
     */
    public ProductDetailsPage waitUntilLoaded() {
        wait.until(ExpectedConditions.urlContains("inventory-item.html"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(detailsContainer));
        wait.until(ExpectedConditions.visibilityOfElementLocated(productName));
        return this;
    }

    /**
     * Reads the visible information and control state for the selected product.
     *
     * @return immutable snapshot of the product details page
     */
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

    /**
     * Follows the page's Back to products action and waits for the catalog.
     *
     * @return loaded catalog page object
     */
    public InventoryPage backToProducts() {
        wait.until(ExpectedConditions.elementToBeClickable(backToProductsButton)).click();
        return new InventoryPage(driver).waitUntilLoaded();
    }

    /**
     * Immutable snapshot of the product information and action on its details page.
     *
     * @param name visible product name
     * @param description visible product description
     * @param price visible product price text
     * @param imageAlt accessible alternative text of the product image
     * @param imageDisplayed whether the product image is visible
     * @param imageLoaded whether the browser successfully loaded the image
     * @param cartButtonLabel visible label of the add/remove button
     * @param cartButtonEnabled whether the add/remove button is enabled
     */
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
