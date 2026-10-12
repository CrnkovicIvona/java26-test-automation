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

/**
 * Page Object Model representation of the authenticated SauceDemo catalog.
 *
 * <p>It provides catalog readiness checks and the logout interaction used by
 * login and special-account tests.
 */
public class InventoryPage {

    /** Browser session used to locate and interact with catalog elements. */
    private final WebDriver driver;

    /** Explicit wait used for page-load and menu interactions. */
    private final WebDriverWait wait;

    /** Stable application test attribute identifying the page title. */
    private final By title = By.cssSelector("[data-test='title']");

    /** Stable application test attribute identifying the product list. */
    private final By inventoryList = By.cssSelector("[data-test='inventory-list']");

    /** Stable application test attribute identifying an individual product card. */
    private final By productCards = By.cssSelector("[data-test='inventory-item']");

    /** Stable application test attribute identifying each product image. */
    private final By productImage = By.cssSelector("[data-test^='inventory-item-'][data-test$='-img']");

    /** Stable application test attribute identifying each product name. */
    private final By productName = By.cssSelector("[data-test='inventory-item-name']");

    /** Stable application test attribute identifying each product description. */
    private final By productDescription = By.cssSelector("[data-test='inventory-item-desc']");

    /** Stable application test attribute identifying each product price. */
    private final By productPrice = By.cssSelector("[data-test='inventory-item-price']");

    /** Identifies the product's add or remove button, scoped to its card. */
    private final By productCartButton = By.cssSelector(
            "[data-test^='add-to-cart-'], [data-test^='remove-']");

    /** Stable application test attribute identifying the shopping cart link. */
    private final By shoppingCart = By.cssSelector("[data-test='shopping-cart-link']");

    /** Stable application test attribute identifying the cart item-count badge. */
    private final By shoppingCartBadge = By.cssSelector("[data-test='shopping-cart-badge']");

    /** Stable application test attribute identifying the product sort container. */
    private final By sortContainer = By.cssSelector("[data-test='product-sort-container']");

    /** Stable application test attribute identifying the displayed sort label. */
    private final By activeSortOption = By.cssSelector("[data-test='active-option']");

    /** Application button that opens the navigation sidebar. */
    private final By menuButton = By.id("react-burger-menu-btn");

    /** Stable application test attribute identifying the sidebar logout link. */
    private final By logoutLink = By.cssSelector("[data-test='logout-sidebar-link']");

    /** Stable application test attribute identifying the sidebar reset link. */
    private final By resetLink = By.cssSelector("[data-test='reset-sidebar-link']");

    /**
     * Creates the page object and its explicit wait for the supplied browser.
     *
     * @param driver active browser session controlled by the current test
     */
    public InventoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TestConfig.TIMEOUT);
    }

    /**
     * Waits for the browser to reach the catalog URL and display the product list.
     *
     * @return this page object to allow fluent wait-and-act calls
     */
    public InventoryPage waitUntilLoaded() {
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryList));
        return this;
    }

    /**
     * Reads the visible catalog page title.
     *
     * @return displayed text of the catalog title element
     */
    public String title() {
        return driver.findElement(title).getText();
    }

    /**
     * Opens the details page for the product with the exact visible name.
     *
     * @param name exact visible name of the product to open
     * @return loaded product details page object
     * @throws NoSuchElementException if no product with the supplied name exists
     */
    public ProductDetailsPage openProductDetails(String name) {
        var nameLink = wait.until(ExpectedConditions.elementToBeClickable(
                findProductCard(name).findElement(productName)));
        nameLink.click();
        return new ProductDetailsPage(driver).waitUntilLoaded();
    }

    /**
     * Reads the visible details of every product card currently displayed.
     *
     * <p>The image-loaded flag checks the browser's decoded image width rather
     * than relying only on the presence of an image element in the DOM.
     *
     * @return immutable snapshots of the product cards and their visible state
     */
    public List<ProductDetails> productDetails() {
        return driver.findElements(productCards).stream()
                .map(this::readProductDetails)
                .toList();
    }

    /**
     * Selects a catalog sort option by its HTML option value.
     *
     * <p>Waits until the visible active-option label matches the selected option,
     * confirming that the page has applied the selection before returning.
     *
     * @param optionValue value of one of the product sort dropdown options
     * @throws NoSuchElementException if the dropdown has no option with that value
     */
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

    /**
     * Reads the currently displayed product sort label.
     *
     * @return visible label for the selected sort option
     */
    public String activeSortOption() {
        return driver.findElement(activeSortOption).getText();
    }

    /**
     * Adds the named product to the cart and waits for the cart badge to appear.
     *
     * @param name exact visible name of the product to add
     * @throws NoSuchElementException if no product with the supplied name exists
     */
    public void addProductToCart(String name) {
        var card = findProductCard(name);
        wait.until(ExpectedConditions.elementToBeClickable(
                card.findElement(productCartButton))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(shoppingCartBadge));
    }

    /**
     * Opens the shopping cart and waits for its contents to load.
     *
     * @return loaded shopping cart page object
     */
    public CartPage openCart() {
        wait.until(ExpectedConditions.elementToBeClickable(shoppingCart)).click();
        return new CartPage(driver).waitUntilLoaded();
    }

    /**
     * Returns the number of products shown in the shopping-cart badge.
     *
     * @return cart item count, or zero when the empty cart has no badge
     */
    public int shoppingCartItemCount() {
        var badges = driver.findElements(shoppingCartBadge);
        return badges.isEmpty() ? 0 : Integer.parseInt(badges.get(0).getText());
    }

    /**
     * Reads the current add/remove button label for the named product.
     *
     * @param name exact visible name of the product
     * @return current label of that product's cart button
     * @throws NoSuchElementException if no product with the supplied name exists
     */
    public String productCartButtonLabel(String name) {
        return findProductCard(name).findElement(productCartButton).getText();
    }

    /**
     * Extracts visible card data and image state from one product card.
     *
     * @param card product card element whose details are read
     * @return snapshot of the card's current displayed data
     */
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

    /**
     * Finds the product card with the exact visible name.
     *
     * @param name exact visible product name to find
     * @return matching product card
     * @throws NoSuchElementException if no card has the supplied name
     */
    private WebElement findProductCard(String name) {
        return driver.findElements(productCards).stream()
                .filter(card -> card.findElement(productName).getText().equals(name))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "No inventory product found with name: " + name));
    }

    /** Opens the navigation sidebar and follows its logout link. */
    public void logout() {
        driver.findElement(menuButton).click();
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();
    }

    /**
     * Immutable snapshot of the information and controls shown on one product card.
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