package tests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pages.ProductDetailsPage;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that a product details page is consistent with its catalog entry.
 */
@Tag("product-details")
class ProductDetailsTest extends StoreTestBase {

    /** Confirms the details content, product image accessibility, and return action. */
    @Test
    @Tag("smoke")
    void productDetailsShouldMatchCatalogAndReturnToProducts() {
        var catalogProduct = inventoryPage.productDetails().stream()
                .filter(product -> product.name().equals(BACKPACK))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Expected catalog product was not present: " + BACKPACK));

        ProductDetailsPage detailsPage = inventoryPage.openProductDetails(BACKPACK);
        var details = detailsPage.details();

        assertAll("Product details: " + BACKPACK,
                () -> assertEquals(catalogProduct.name(), details.name(),
                        "Expected and actual product names should match"),
                () -> assertEquals(catalogProduct.description(), details.description(),
                        "Expected and actual product descriptions should match"),
                () -> assertEquals(catalogProduct.price(), details.price(),
                        "Expected and actual product prices should match"),
                () -> assertEquals(catalogProduct.imageAlt(), details.imageAlt(),
                        "The image alternative text should identify the product"),
                () -> assertTrue(details.imageDisplayed(),
                        "The product image should be visible"),
                () -> assertTrue(details.imageLoaded(),
                        "The product image should be loaded"),
                () -> assertEquals("Add to cart", details.cartButtonLabel(),
                        "The product should initially be available to add"),
                () -> assertTrue(details.cartButtonEnabled(),
                        "The product action should be enabled"));

        inventoryPage = detailsPage.backToProducts();
        assertEquals("Products", inventoryPage.title(),
                "The back action should return to the product catalog");
        assertTrue(inventoryPage.productDetails().stream()
                        .anyMatch(product -> product.name().equals(BACKPACK)),
                "The catalog should still display the selected product");
    }
}
