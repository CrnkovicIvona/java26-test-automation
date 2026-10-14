package tests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pages.CartPage;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Tag("cart")
class CartTest extends StoreTestBase {

    
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

    
    @Test
    @Tag("smoke")
    void addedProductShouldAppearWithCorrectCartDetails() {
        var expectedProduct = productDetailsFor(BACKPACK);
        var cartPage = openCartWithProducts(BACKPACK);
        var item = cartPage.items().getFirst();

        assertAll("Cart item: " + BACKPACK,
                () -> assertEquals(1, cartPage.itemCount(),
                        "Exactly one product row should be displayed"),
                () -> assertEquals(1, item.quantity(),
                        "The cart quantity should be one"),
                () -> assertEquals(BACKPACK, item.name(),
                        "The cart row should display the added product"),
                () -> assertEquals(expectedProduct.description(), item.description(),
                        "The cart row should display the catalog description"),
                () -> assertEquals("$29.99", item.price(),
                        "The cart row should display the catalog price"),
                () -> assertEquals("Remove", item.removeButtonLabel(),
                        "The cart row should provide a Remove action"),
                () -> assertTrue(item.removeButtonEnabled(),
                        "The Remove action should be enabled"),
                () -> assertEquals(1, cartPage.badgeCount(),
                        "The cart badge should match the number of items"));
    }

    @Test
    @Tag("regression")
    void multipleProductsShouldAppearInCart() {
        var expectedBackpack = productDetailsFor(BACKPACK);
        var expectedBikeLight = productDetailsFor(BIKE_LIGHT);
        var cartPage = openCartWithProducts(BACKPACK, BIKE_LIGHT);
        var items = cartPage.items();

        assertAll("Multiple cart items",
                () -> assertEquals(2, cartPage.itemCount(),
                        "The cart should contain one row for each selected product"),
                () -> assertEquals(Set.of(BACKPACK, BIKE_LIGHT),
                        items.stream().map(CartPage.CartItem::name).collect(Collectors.toSet()),
                        "The cart should contain exactly the selected products"),
                () -> assertTrue(items.stream().allMatch(item -> item.quantity() == 1),
                        "Each selected product should have a quantity of one"),
                () -> assertEquals(expectedBackpack.description(),
                        items.stream().filter(item -> item.name().equals(BACKPACK))
                                .findFirst().orElseThrow().description(),
                        "The backpack row should display its catalog description"),
                () -> assertEquals(expectedBikeLight.description(),
                        items.stream().filter(item -> item.name().equals(BIKE_LIGHT))
                                .findFirst().orElseThrow().description(),
                        "The bike-light row should display its catalog description"),
                () -> assertEquals(2, cartPage.badgeCount(),
                        "The cart badge should match the number of products"));
    }

    
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
