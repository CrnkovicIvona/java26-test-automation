package tests;

import data.TestUsers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pages.InventoryPage;
import pages.LoginPage;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Verifies that the product catalog displays complete product cards and that
 * adding a product updates the cart state.
 */
@Tag("inventory")
class InventoryTest extends BaseTest {

    /** Expected catalog size from the supplied SauceDemo inventory markup. */
    private static final int EXPECTED_PRODUCT_COUNT = 6;

    /** Product used to verify the add-to-cart interaction. */
    private static final String PRODUCT_TO_ADD = "Sauce Labs Backpack";

    /** Page object for catalog checks and cart interactions. */
    private InventoryPage inventoryPage;

    /** Signs in with the standard account and waits for the catalog to load. */
    @BeforeEach
    void openInventoryPage() {
        new LoginPage(driver).open().loginAs(TestUsers.STANDARD);
        inventoryPage = new InventoryPage(driver).waitUntilLoaded();
    }

    /**
     * Confirms every product card has a loaded, accessible image; non-empty
     * name, description, and price; and an enabled add-to-cart button.
     */
    @Test
    @Tag("smoke")
    void everyProductShouldDisplayCompleteDetails() {
        var products = inventoryPage.productDetails();

        assertEquals(EXPECTED_PRODUCT_COUNT, products.size(),
                "The catalog should display all expected product cards");

        for (var product : products) {
            assertAll("Product: " + product.name(),
                    () -> assertFalse(product.name().isBlank(),
                            "The product name must not be empty"),
                    () -> assertFalse(product.description().isBlank(),
                            "The product description must not be empty"),
                    () -> assertTrue(product.price().matches("\\$\\d+\\.\\d{2}"),
                            "The product price must use the expected currency format"),
                    () -> assertEquals(product.name(), product.imageAlt(),
                            "The image alternative text should identify the product"),
                    () -> assertTrue(product.imageDisplayed(),
                            "The product image must be visible"),
                    () -> assertTrue(product.imageLoaded(),
                            "The product image must load successfully"),
                    () -> assertEquals("Add to cart", product.cartButtonLabel(),
                            "An unselected product should have an Add to cart button"),
                    () -> assertTrue(product.cartButtonEnabled(),
                            "The Add to cart button must be enabled"));
        }
    }

    /** Confirms adding a product increments the cart badge and changes its button. */
    @Test
    @Tag("smoke")
    void addingProductShouldUpdateCartCountAndButton() {
        assertEquals(0, inventoryPage.shoppingCartItemCount(),
                "A new browser session should start with an empty cart");

        inventoryPage.addProductToCart(PRODUCT_TO_ADD);

        assertEquals(1, inventoryPage.shoppingCartItemCount(),
                "Adding one product should display one item in the cart");
        assertEquals("Remove", inventoryPage.productCartButtonLabel(PRODUCT_TO_ADD),
                "The added product button should change to Remove");
    }

    /**
     * Checks each sort option against the actual ordered product names and prices.
     * The equal-priced T-shirts are also asserted in their expected stable order.
     *
     * @param optionValue value sent to the product sort dropdown
     * @param expectedLabel label displayed for the selected sort option
     * @param expectedNames expected sequence of product names after sorting
     * @param expectedPrices expected sequence of prices after sorting
     */
    @ParameterizedTest(name = "{1}")
    @MethodSource("productSortScenarios")
    @Tag("smoke")
    void productsShouldBeSortedBySelectedOption(
            String optionValue,
            String expectedLabel,
            List<String> expectedNames,
            List<String> expectedPrices) {
        inventoryPage.sortProducts(optionValue);

        var sortedProducts = inventoryPage.productDetails();

        assertAll("Sort option: " + expectedLabel,
                () -> assertEquals(expectedLabel, inventoryPage.activeSortOption(),
                        "The active sort label should match the selected option"),
                () -> assertEquals(expectedNames,
                        sortedProducts.stream().map(product -> product.name()).toList(),
                        "The product names should appear in the expected sorted sequence"),
                () -> assertEquals(expectedPrices,
                        sortedProducts.stream().map(product -> product.price()).toList(),
                        "The product prices should appear in the expected sorted sequence"));
    }

    /**
     * Supplies expected catalog sequences for all four SauceDemo sort options.
     *
     * @return scenarios containing the dropdown value, label, and expected data order
     */
    static Stream<Arguments> productSortScenarios() {
        return Stream.of(
                arguments("az", "Name (A to Z)",
                        List.of(
                                "Sauce Labs Backpack",
                                "Sauce Labs Bike Light",
                                "Sauce Labs Bolt T-Shirt",
                                "Sauce Labs Fleece Jacket",
                                "Sauce Labs Onesie",
                                "Test.allTheThings() T-Shirt (Red)"),
                        List.of("$29.99", "$9.99", "$15.99", "$49.99", "$7.99", "$15.99")),
                arguments("za", "Name (Z to A)",
                        List.of(
                                "Test.allTheThings() T-Shirt (Red)",
                                "Sauce Labs Onesie",
                                "Sauce Labs Fleece Jacket",
                                "Sauce Labs Bolt T-Shirt",
                                "Sauce Labs Bike Light",
                                "Sauce Labs Backpack"),
                        List.of("$15.99", "$7.99", "$49.99", "$15.99", "$9.99", "$29.99")),
                arguments("lohi", "Price (low to high)",
                        List.of(
                                "Sauce Labs Onesie",
                                "Sauce Labs Bike Light",
                                "Sauce Labs Bolt T-Shirt",
                                "Test.allTheThings() T-Shirt (Red)",
                                "Sauce Labs Backpack",
                                "Sauce Labs Fleece Jacket"),
                        List.of("$7.99", "$9.99", "$15.99", "$15.99", "$29.99", "$49.99")),
                arguments("hilo", "Price (high to low)",
                        List.of(
                                "Sauce Labs Fleece Jacket",
                                "Sauce Labs Backpack",
                                "Sauce Labs Bolt T-Shirt",
                                "Test.allTheThings() T-Shirt (Red)",
                                "Sauce Labs Bike Light",
                                "Sauce Labs Onesie"),
                        List.of("$49.99", "$29.99", "$15.99", "$15.99", "$9.99", "$7.99")));
    }
}
