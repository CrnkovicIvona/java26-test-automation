package pages;

import config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object Model representation of SauceDemo's completed checkout page.
 *
 * <p>It exposes the order confirmation, delivery message, confirmation image,
 * cart badge, and the available navigation and order-export actions.
 */
public class CheckoutCompletePage {

    /** Browser session used to locate and interact with page elements. */
    private final WebDriver driver;

    /** Explicit wait used to synchronize the confirmation page and its actions. */
    private final WebDriverWait wait;

    /** Stable application test attribute identifying the page title. */
    private final By title = By.cssSelector("[data-test='title']");

    /** Stable application test attribute identifying the completion content. */
    private final By completeContainer =
            By.cssSelector("[data-test='checkout-complete-container']");

    /** Stable application test attribute identifying the order confirmation heading. */
    private final By confirmationHeading =
            By.cssSelector("[data-test='complete-header']");

    /** Stable application test attribute identifying the order dispatch message. */
    private final By confirmationMessage =
            By.cssSelector("[data-test='complete-text']");

    /** Stable application test attribute identifying the delivery illustration. */
    private final By deliveryImage = By.cssSelector("[data-test='pony-express']");

    /** Stable application test attribute identifying the cart item-count badge. */
    private final By cartBadge = By.cssSelector("[data-test='shopping-cart-badge']");

    /** Stable application test attribute identifying the return-to-catalog button. */
    private final By backToProductsButton =
            By.cssSelector("[data-test='back-to-products']");

    /** Stable application test attribute identifying the order PDF button. */
    private final By generatePdfButton =
            By.cssSelector("[data-test='generate-pdf-order']");

    /**
     * Creates the page object and its explicit wait for the supplied browser.
     *
     * @param driver active browser session controlled by the current test
     */
    public CheckoutCompletePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TestConfig.TIMEOUT);
    }

    /**
     * Waits until the browser reaches the completed checkout page.
     *
     * @return this page object to allow fluent wait-and-read calls
     */
    public CheckoutCompletePage waitUntilLoaded() {
        wait.until(ExpectedConditions.urlContains("checkout-complete.html"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(completeContainer));
        wait.until(ExpectedConditions.visibilityOfElementLocated(confirmationHeading));
        wait.until(ExpectedConditions.visibilityOfElementLocated(deliveryImage));
        return this;
    }

    /**
     * Reads the visible checkout-complete page title.
     *
     * @return displayed text of the page title
     */
    public String title() {
        return driver.findElement(title).getText();
    }

    /**
     * Reads the order confirmation heading.
     *
     * @return displayed confirmation heading
     */
    public String confirmationHeading() {
        return driver.findElement(confirmationHeading).getText();
    }

    /**
     * Reads the order dispatch message.
     *
     * @return displayed delivery confirmation message
     */
    public String confirmationMessage() {
        return driver.findElement(confirmationMessage).getText();
    }

    /**
     * Reads the alternative text for the delivery illustration.
     *
     * @return image alternative text
     */
    public String deliveryImageAltText() {
        return driver.findElement(deliveryImage).getAttribute("alt");
    }

    /**
     * Checks whether the delivery illustration is visible.
     *
     * @return {@code true} if the image is displayed; otherwise {@code false}
     */
    public boolean isDeliveryImageDisplayed() {
        return driver.findElement(deliveryImage).isDisplayed();
    }

    /**
     * Reads the cart badge count in the confirmation page header.
     *
     * @return numeric count shown in the badge, or zero when the badge is absent
     */
    public int cartBadgeCount() {
        var badges = driver.findElements(cartBadge);
        return badges.isEmpty() ? 0 : Integer.parseInt(badges.get(0).getText());
    }

    /**
     * Returns to the product catalog and waits for it to load.
     *
     * @return loaded product catalog page object
     */
    public InventoryPage backToProducts() {
        wait.until(ExpectedConditions.elementToBeClickable(backToProductsButton)).click();
        return new InventoryPage(driver).waitUntilLoaded();
    }

    /**
     * Starts downloading the completed order as a PDF.
     */
    public void generatePdfOrder() {
        wait.until(ExpectedConditions.elementToBeClickable(generatePdfButton)).click();
    }
}
