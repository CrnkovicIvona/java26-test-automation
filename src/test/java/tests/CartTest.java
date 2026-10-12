package tests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pages.CartPage;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies cart contents, item removal, and cart navigation.
 */
@Tag("cart")
class CartTest extends StoreTestBase {

    /** Confirms a new independent session opens an empty cart without a badge. */
    @Test
    @Tag("regression")
    void emptyCartShouldHaveNoItemsOrBadge() {
        CartPage cartPage = inventoryPage.openCart();

        assertAll("Empty cart state",
                () -> assertEquals("Your Cart", cartPage.title(),
                        "The cart page should be displayed"),
                () -> assertTrue(cartPage.isEmpty(),
                        "A fresh browser session should have no cart items"),
                () -> assertEquals(0, cartPage.badgeCount(),
                        "An empty cart should not show a count badge"));
    }

    /** Checks that a cart row preserves the expected product data and quantity. */
    @Test
    @Tag("smoke")
    void addedProductShouldAppearWithCorrectCartDetails() {
        var cartPage = openCartWithProducts(BACKPACK);
        var item = cartPage.items().getFirst();

        assertAll("Cart item: " + BACKPACK,
                () -> assertEquals(1, cartPage.itemCount(),
                        "Exactly one product row should be displayed"),
                () -> assertEquals(1, item.quantity(),
                        "The cart quantity should be one"),
                () -> assertEquals(BACKPACK, item.name(),
                        "The cart row should display the added product"),
                () -> assertEquals("$29.99", item.price(),
                        "The cart row should display the catalog price"),
                () -> assertEquals("Remove", item.removeButtonLabel(),
                        "The cart row should provide a Remove action"),
                () -> assertTrue(item.removeButtonEnabled(),
                        "The Remove action should be enabled"),
                () -> assertEquals(1, cartPage.badgeCount(),
                        "The cart badge should match the number of items"));
    }

    /** Confirms removing one item from a two-product cart preserves the other. */
    @Test
    @Tag("regression")
    void removingProductShouldUpdateCartContentsAndBadge() {
        var cartPage = openCartWithProducts(BACKPACK, BIKE_LIGHT);
        cartPage.removeProduct(BACKPACK);

        assertAll("Cart after removing " + BACKPACK,
                () -> assertEquals(1, cartPage.itemCount(),
                        "One product row should remain"),
                () -> assertEquals(BIKE_LIGHT, cartPage.items().getFirst().name(),
                        "The other product should remain in the cart"),
                () -> assertEquals(1, cartPage.badgeCount(),
                        "The cart badge should reflect the remaining item"));

        cartPage.removeProduct(BIKE_LIGHT);
        assertAll("Cart after removing the final product",
                () -> assertTrue(cartPage.isEmpty(),
                        "The cart should have no product rows"),
                () -> assertEquals(0, cartPage.badgeCount(),
                        "The badge should disappear when the cart becomes empty"));
    }

    /** Checks that Continue Shopping returns to the catalog and retains cart state. */
    @Test
    @Tag("regression")
    void continueShoppingShouldPreserveCartContents() {
        var cartPage = openCartWithProducts(BACKPACK);
        var returnedInventory = cartPage.continueShopping();

        assertAll("Catalog after Continue Shopping",
                () -> assertEquals("Products", returnedInventory.title(),
                        "Continue Shopping should return to the catalog"),
                () -> assertEquals(1, returnedInventory.shoppingCartItemCount(),
                        "Returning to the catalog should preserve the cart badge"),
                () -> assertEquals("Remove",
                        returnedInventory.productCartButtonLabel(BACKPACK),
                        "The added product should remain selected"));
    }

    /** Confirms Checkout opens step one with the cart's current item count. */
    @Test
    @Tag("regression")
    void checkoutShouldOpenCustomerInformationWithCartState() {
        var checkoutPage = openCartWithProducts(BACKPACK, BIKE_LIGHT)
                .proceedToCheckout();

        assertAll("Checkout step one",
                () -> assertEquals("Checkout: Your Information", checkoutPage.title(),
                        "The customer information page should be displayed"),
                () -> assertEquals(2, checkoutPage.cartBadgeCount(),
                        "Checkout should preserve the cart item count"));
    }
}
