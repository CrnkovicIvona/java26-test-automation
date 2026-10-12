package tests;

import data.CheckoutTestData;
import data.TestUsers;
import org.junit.jupiter.api.BeforeEach;
import pages.CartPage;
import pages.CheckoutStepOnePage;
import pages.CheckoutStepTwoPage;
import pages.InventoryPage;
import pages.LoginPage;

/**
 * Signs in each test with the standard account and provides common store setup.
 *
 * <p>The browser itself is created by {@link BaseTest}; this class keeps the
 * repeated login and cart setup consistent without sharing state between tests.
 */
abstract class StoreTestBase extends BaseTest {

    /** A stable product used by catalog, cart, and checkout scenarios. */
    protected static final String BACKPACK = "Sauce Labs Backpack";

    /** A second distinct product used to verify multiple-item totals. */
    protected static final String BIKE_LIGHT = "Sauce Labs Bike Light";

    /** Loaded catalog page available to each test after setup. */
    protected InventoryPage inventoryPage;

    /** Authenticates with the public test account and waits for the catalog. */
    @BeforeEach
    void signInToStore() {
        new LoginPage(driver).open().loginAs(TestUsers.STANDARD);
        inventoryPage = new InventoryPage(driver).waitUntilLoaded();
    }

    /**
     * Adds each requested product, then opens the cart through the catalog UI.
     *
     * @param productNames exact product names to add before opening the cart
     * @return loaded cart page object
     */
    protected CartPage openCartWithProducts(String... productNames) {
        for (var productName : productNames) {
            inventoryPage.addProductToCart(productName);
        }
        return inventoryPage.openCart();
    }

    /**
     * Opens checkout step one after adding the requested products to the cart.
     *
     * @param productNames exact product names to add before checkout
     * @return loaded first checkout step page object
     */
    protected CheckoutStepOnePage openCheckoutWithProducts(String... productNames) {
        return openCartWithProducts(productNames).proceedToCheckout();
    }

    /**
     * Enters stable customer data and advances to the checkout overview.
     *
     * @param checkoutPage loaded first checkout step page
     * @return loaded checkout overview page object
     */
    protected CheckoutStepTwoPage enterValidCustomerInformation(
            CheckoutStepOnePage checkoutPage) {
        var customer = CheckoutTestData.randomCustomer();
        checkoutPage.enterFirstName(customer.firstName());
        checkoutPage.enterLastName(customer.lastName());
        checkoutPage.enterPostalCode(customer.postalCode());
        return checkoutPage.continueCheckout();
    }
}
