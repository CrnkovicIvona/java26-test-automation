package tests;

import data.CheckoutTestData;
import data.TestUsers;
import org.junit.jupiter.api.BeforeEach;
import pages.CartPage;
import pages.CheckoutStepOnePage;
import pages.CheckoutStepTwoPage;
import pages.InventoryPage;
import pages.LoginPage;


abstract class StoreTestBase extends BaseTest {

    
    protected static final String BACKPACK = "Sauce Labs Backpack";

    
    protected static final String BIKE_LIGHT = "Sauce Labs Bike Light";

    
    protected InventoryPage inventoryPage;

    
    @BeforeEach
    void signInToStore() {
        new LoginPage(driver).open().loginAs(TestUsers.STANDARD);
        inventoryPage = new InventoryPage(driver).waitUntilLoaded();
    }

    
    protected CartPage openCartWithProducts(String... productNames) {
        for (var productName : productNames) {
            inventoryPage.addProductToCart(productName);
        }
        return inventoryPage.openCart();
    }

    protected InventoryPage.ProductDetails productDetailsFor(String productName) {
        return inventoryPage.productDetails().stream()
                .filter(product -> product.name().equals(productName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Expected catalog product was not present: " + productName));
    }

    protected CheckoutStepOnePage openCheckoutWithProducts(String... productNames) {
        return openCartWithProducts(productNames).proceedToCheckout();
    }

    
    protected CheckoutStepTwoPage enterValidCustomerInformation(
            CheckoutStepOnePage checkoutPage) {
        var customer = CheckoutTestData.randomCustomer();
        checkoutPage.enterFirstName(customer.firstName());
        checkoutPage.enterLastName(customer.lastName());
        checkoutPage.enterPostalCode(customer.postalCode());
        return checkoutPage.continueCheckout();
    }
}
