package tests;

import data.TestUser;
import data.TestUsers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pages.InventoryPage;
import pages.LoginPage;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Keeps SauceDemo's intentionally unusual account scenarios separate from
 * standard smoke and regression tests.
 *
 * <p>These checks currently confirm that each account reaches the catalog.
 * They do not assert a particular visual, sorting, or performance defect.
 */
@Tag("special-user")
class ProblematicUserTest extends BaseTest {

    /** Page object used to submit the selected account's credentials. */
    private LoginPage loginPage;

    /** Page object used to wait for and inspect the product catalog. */
    private InventoryPage inventoryPage;

    /** Opens a clean login page and prepares page objects for each scenario. */
    @BeforeEach
    void openLoginPage() {
        loginPage = new LoginPage(driver).open();
        inventoryPage = new InventoryPage(driver);
    }

    /** Checks that the problem account can sign in and load the catalog. */
    @Test
    void problemUserCanOpenCatalogForDefectInvestigation() {
        assertCatalogOpens(TestUsers.PROBLEM);
    }

    /** Checks catalog access for the account that may respond more slowly. */
    @Test
    void performanceGlitchUserCanOpenCatalogForDefectInvestigation() {
        assertCatalogOpens(TestUsers.PERFORMANCE_GLITCH);
    }

    /** Checks that the error account can sign in and load the catalog. */
    @Test
    void errorUserCanOpenCatalogForDefectInvestigation() {
        assertCatalogOpens(TestUsers.ERROR);
    }

    /** Checks catalog access for the account intended for visual investigation. */
    @Test
    void visualUserCanOpenCatalogForDefectInvestigation() {
        assertCatalogOpens(TestUsers.VISUAL);
    }

    /**
     * Signs in with the supplied account and verifies that the catalog is available.
     *
     * @param user test account whose catalog access is being checked
     */
    private void assertCatalogOpens(TestUser user) {
        loginPage.loginAs(user);

        inventoryPage.waitUntilLoaded();
        assertEquals("Products", inventoryPage.title());
    }
}
