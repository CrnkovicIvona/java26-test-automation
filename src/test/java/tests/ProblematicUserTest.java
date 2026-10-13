package tests;

import data.TestUser;
import data.TestUsers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pages.InventoryPage;
import pages.LoginPage;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Tag("special-user")
class ProblematicUserTest extends BaseTest {

    
    private LoginPage loginPage;

    
    private InventoryPage inventoryPage;

    
    @BeforeEach
    void openLoginPage() {
        loginPage = new LoginPage(driver).open();
        inventoryPage = new InventoryPage(driver);
    }

    
    @Test
    void problemUserCanOpenCatalogForDefectInvestigation() {
        assertCatalogOpens(TestUsers.PROBLEM);
    }

    
    @Test
    void performanceGlitchUserCanOpenCatalogForDefectInvestigation() {
        assertCatalogOpens(TestUsers.PERFORMANCE_GLITCH);
    }

    
    @Test
    void errorUserCanOpenCatalogForDefectInvestigation() {
        assertCatalogOpens(TestUsers.ERROR);
    }

    
    @Test
    void visualUserCanOpenCatalogForDefectInvestigation() {
        assertCatalogOpens(TestUsers.VISUAL);
    }

    
    private void assertCatalogOpens(TestUser user) {
        loginPage.loginAs(user);

        inventoryPage.waitUntilLoaded();
        assertEquals("Products", inventoryPage.title());
    }
}
