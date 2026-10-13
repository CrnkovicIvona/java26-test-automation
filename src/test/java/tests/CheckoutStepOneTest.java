package tests;

import data.CheckoutTestData;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pages.CheckoutStepOnePage;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Tag("checkout-step-one")
class CheckoutStepOneTest extends StoreTestBase {

    
    @ParameterizedTest(name = "Missing {0} should be rejected")
    @MethodSource("data.CheckoutTestData#missingCustomerFields")
    @Tag("regression")
    void missingRequiredCustomerFieldShouldShowValidation(
            String fieldName,
            String expectedError,
            String firstName,
            String lastName,
            String postalCode) {
        var checkoutPage = openCheckoutWithProducts(BACKPACK);
        checkoutPage.enterFirstName(firstName);
        checkoutPage.enterLastName(lastName);
        checkoutPage.enterPostalCode(postalCode);

        checkoutPage.submitCustomerInformation();

        assertEquals(expectedError, checkoutPage.errorMessage(),
                "Expected a validation message for the missing " + fieldName);
        assertTrue(driver.getCurrentUrl().contains("checkout-step-one.html"),
                "Invalid customer information should keep the user on checkout step one");
    }

    
    @Test
    @Tag("regression")
    void validCustomerInformationShouldOpenCheckoutOverview() {
        var checkoutPage = openCheckoutWithProducts(BACKPACK);
        var overviewPage = enterValidCustomerInformation(checkoutPage);

        assertAll("Checkout overview after valid customer information",
                () -> assertEquals("Checkout: Overview", overviewPage.title(),
                        "Valid customer information should advance to the overview"),
                () -> assertEquals(1, overviewPage.cartBadgeCount(),
                        "The selected cart item should remain in checkout"));
    }

    
    @Test
    @Tag("regression")
    void customerFieldsShouldHaveAccessibleNamesAndLogicalKeyboardFocus() {
        CheckoutStepOnePage checkoutPage = openCheckoutWithProducts(BACKPACK);

        assertAll("Customer field accessible names",
                () -> assertEquals("First Name", checkoutPage.firstNameAccessibleName(),
                        "The first-name field should have an accessible name"),
                () -> assertEquals("Last Name", checkoutPage.lastNameAccessibleName(),
                        "The last-name field should have an accessible name"),
                () -> assertEquals("Zip/Postal Code",
                        checkoutPage.postalCodeAccessibleName(),
                        "The postal-code field should have an accessible name"));

        checkoutPage.tabForwardFromFirstName();
        assertAll("Keyboard focus after first field",
                () -> assertEquals("Last Name",
                        checkoutPage.focusedElementAccessibleName(),
                        "Tab should move focus from first name to last name"),
                () -> assertTrue(checkoutPage.isFocusedElementFocusVisible(),
                        "Keyboard focus should be visibly indicated"));

        checkoutPage.tabForwardFromLastName();
        assertEquals("Zip/Postal Code", checkoutPage.focusedElementAccessibleName(),
                "Tab should move focus from last name to postal code");
    }

    
    @Test
    @Tag("regression")
    void cancelShouldReturnToCart() {
        var checkoutPage = openCheckoutWithProducts(BACKPACK);
        var cartPage = checkoutPage.cancel();

        assertAll("Cart after cancelling checkout",
                () -> assertEquals("Your Cart", cartPage.title(),
                        "Cancel should return to the cart"),
                () -> assertEquals(1, cartPage.badgeCount(),
                        "Cancelling checkout should preserve the cart"),
                () -> assertEquals(BACKPACK, cartPage.items().getFirst().name(),
                        "The cart should still contain the selected product"));
    }
}
