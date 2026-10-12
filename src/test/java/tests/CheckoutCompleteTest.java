package tests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the successful order completion and confirmation page.
 */
@Tag("checkout-complete")
class CheckoutCompleteTest extends StoreTestBase {

    /** Runs the successful checkout flow and verifies the resulting confirmation. */
    @Test
    @Tag("smoke")
    void successfulCheckoutShouldDisplayOrderConfirmation() {
        var checkoutPage = openCheckoutWithProducts(BACKPACK);
        var overviewPage = enterValidCustomerInformation(checkoutPage);
        var completePage = overviewPage.finishCheckout();

        assertAll("Completed order",
                () -> assertEquals("Checkout: Complete!", completePage.title(),
                        "The completed checkout page should be displayed"),
                () -> assertEquals("Thank you for your order!",
                        completePage.confirmationHeading(),
                        "The page should confirm that the order was received"),
                () -> assertEquals(
                        "Your order has been dispatched, and will arrive just as fast as the pony can get there!",
                        completePage.confirmationMessage(),
                        "The page should display its order-dispatch message"),
                () -> assertTrue(completePage.isDeliveryImageDisplayed(),
                        "The delivery illustration should be visible"),
                () -> assertEquals("Pony Express",
                        completePage.deliveryImageAltText(),
                        "The delivery illustration should have useful alternative text"),
                () -> assertEquals(0, completePage.cartBadgeCount(),
                        "Completing the order should clear the cart badge"));

        var returnedInventory = completePage.backToProducts();
        assertAll("Catalog after completed order",
                () -> assertEquals("Products", returnedInventory.title(),
                        "Back Home should return to the product catalog"),
                () -> assertEquals(0, returnedInventory.shoppingCartItemCount(),
                        "The completed order should leave the cart empty"));
    }
}
