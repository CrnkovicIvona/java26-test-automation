package tests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pages.CheckoutStepTwoPage;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Tag("checkout-step-two")
class CheckoutStepTwoTest extends StoreTestBase {

    
    @Test
    @Tag("smoke")
    void overviewShouldShowCorrectItemsAndCalculatedTotals() {
        var expectedBackpack = productDetailsFor(BACKPACK);
        var expectedBikeLight = productDetailsFor(BIKE_LIGHT);
        CheckoutStepTwoPage overviewPage = enterValidCustomerInformation(
                openCheckoutWithProducts(BACKPACK, BIKE_LIGHT));
        var items = overviewPage.items();

        assertAll("Checkout overview",
                () -> assertEquals(2, overviewPage.itemCount(),
                        "The overview should contain both selected product rows"),
                () -> assertEquals(BACKPACK, items.get(0).name(),
                        "The first row should match the first selected product"),
                () -> assertEquals(expectedBackpack.description(), items.get(0).description(),
                        "The first row should display the catalog description"),
                () -> assertEquals("$29.99", items.get(0).price(),
                        "The first row should display the catalog price"),
                () -> assertEquals(BIKE_LIGHT, items.get(1).name(),
                        "The second row should match the second selected product"),
                () -> assertEquals(expectedBikeLight.description(), items.get(1).description(),
                        "The second row should display the catalog description"),
                () -> assertEquals("$9.99", items.get(1).price(),
                        "The second row should display the catalog price"),
                () -> assertEquals("SauceCard #31337",
                        overviewPage.paymentInformation(),
                        "The standard payment information should be displayed"),
                () -> assertEquals("Free Pony Express Delivery!",
                        overviewPage.shippingInformation(),
                        "The standard shipping information should be displayed"),
                () -> assertEquals("Item total: $39.98", overviewPage.subtotal(),
                        "Subtotal should equal the sum of the two item prices"),
                () -> assertEquals("Tax: $3.20", overviewPage.tax(),
                        "Tax should match the displayed subtotal calculation"),
                () -> assertEquals("Total: $43.18", overviewPage.total(),
                        "Total should equal subtotal plus tax"),
                () -> assertEquals(2, overviewPage.cartBadgeCount(),
                        "The overview badge should match the order item count"));
    }

    
    @Test
    @Tag("characterization")
    void emptyOrderOverviewShouldShowNoItemsAndZeroTotals() {
        var checkoutPage = openCheckoutWithProducts();
        CheckoutStepTwoPage overviewPage = enterValidCustomerInformation(checkoutPage);

        assertAll("Empty checkout overview",
                () -> assertTrue(overviewPage.items().isEmpty(),
                        "An empty cart should not show order lines"),
                () -> assertEquals(0, overviewPage.itemCount(),
                        "An empty cart should have zero summary rows"),
                () -> assertEquals(0, overviewPage.cartBadgeCount(),
                        "An empty order should not show a cart badge"),
                () -> assertEquals("Item total: $0", overviewPage.subtotal(),
                        "An empty cart should have a zero subtotal"),
                () -> assertEquals("Tax: $0.00", overviewPage.tax(),
                        "An empty cart should have zero tax"),
                () -> assertEquals("Total: $0.00", overviewPage.total(),
                        "An empty cart should have a zero total"));
    }

    
    @Test
    @Tag("regression")
    void cancelShouldReturnToCatalogAndRetainCart() {
        var overviewPage = enterValidCustomerInformation(
                openCheckoutWithProducts(BACKPACK));
        var returnedInventory = overviewPage.cancel();

        assertAll("Catalog after cancelling order overview",
                () -> assertEquals("Products", returnedInventory.title(),
                        "Cancel should return to the product catalog"),
                () -> assertEquals(1, returnedInventory.shoppingCartItemCount(),
                        "The selected product should remain in the cart"),
                () -> assertEquals("Remove",
                        returnedInventory.productCartButtonLabel(BACKPACK),
                        "The selected product should still be marked as added"));
    }

    
    @Test
    @Tag("regression")
    void singleProductOrderShouldShowCorrectTotals() {
        var expectedBackpack = productDetailsFor(BACKPACK);
        var overviewPage = enterValidCustomerInformation(
                openCheckoutWithProducts(BACKPACK));
        var items = overviewPage.items();

        assertAll("Single-product checkout overview",
                () -> assertEquals(1, overviewPage.itemCount(),
                        "The overview should contain one product"),
                () -> assertEquals(BACKPACK, items.getFirst().name(),
                        "The selected product should appear in the overview"),
                () -> assertEquals(expectedBackpack.description(), items.getFirst().description(),
                        "The selected product should display its catalog description"),
                () -> assertEquals("$29.99", items.getFirst().price(),
                        "The product price should match the catalog"),
                () -> assertEquals("Item total: $29.99", overviewPage.subtotal(),
                        "Subtotal should equal the product price"),
                () -> assertEquals("Tax: $2.40", overviewPage.tax(),
                        "Tax should be calculated for one product"),
                () -> assertEquals("Total: $32.39", overviewPage.total(),
                        "Total should equal subtotal plus tax"),
                () -> assertEquals(1, overviewPage.cartBadgeCount(),
                        "The cart badge should match the order item count"));
    }

}
