package tests;

import data.LoginErrors;
import data.TestUsers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import config.TestConfig;
import pages.InventoryPage;
import pages.LoginPage;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;


@Tag("login")
class LoginTest extends BaseTest {

    
    private LoginPage loginPage;

    
    private InventoryPage inventoryPage;

    
    @BeforeEach
    void openLoginPage() {
        loginPage = new LoginPage(driver).open();
        inventoryPage = new InventoryPage(driver);
    }

    
    @Test
    @Tag("smoke")
    void standardUserShouldSeeProductsAfterLogin() {
        loginPage.loginAs(TestUsers.STANDARD);

        inventoryPage.waitUntilLoaded();
        assertEquals("Products", inventoryPage.title());
    }

    
    @Test
    @Tag("regression")
    void lockedOutUserShouldSeeLockedOutMessage() {
        loginPage.loginAs(TestUsers.LOCKED_OUT);

        assertEquals(LoginErrors.LOCKED_OUT, loginPage.errorMessage());
        assertEquals(TestConfig.BASE_URL, loginPage.currentUrl(),
                "A locked-out user must remain on the login page");
    }

    
    @ParameterizedTest(name = "[{index}] {2}")
    @MethodSource("invalidLogins")
    @Tag("regression")
    void invalidLoginShouldShowErrorMessage(String user, String pass, String expectedError) {
        loginPage.login(user, pass);

        assertEquals(expectedError, loginPage.errorMessage());
        assertEquals(TestConfig.BASE_URL, loginPage.currentUrl(),
                "An invalid login must remain on the login page");
    }

    
    static Stream<Arguments> invalidLogins() {
        return Stream.of(
                arguments("wrong_user", "secret_sauce", LoginErrors.INVALID_CREDENTIALS),
                arguments("standard_user", "wrong_password", LoginErrors.INVALID_CREDENTIALS),
                arguments(null, "secret_sauce", LoginErrors.USERNAME_REQUIRED),
                arguments("standard_user", null, LoginErrors.PASSWORD_REQUIRED),
                arguments(null, null, LoginErrors.USERNAME_REQUIRED));
    }

    
    @Test
    @Tag("regression")
    void passwordFieldShouldMaskInput() {
        assertEquals("password", loginPage.passwordFieldType());
    }

    
    @Test
    @Tag("regression")
    void userShouldReturnToLoginPageAfterLogout() {
        loginPage.loginAs(TestUsers.STANDARD);
        inventoryPage.waitUntilLoaded().logout();

        assertTrue(loginPage.isLoginButtonDisplayed(),
                "The login form must be visible after logging out");
        assertEquals(TestConfig.BASE_URL, loginPage.currentUrl());
    }

    @Test
    @Tag("regression")
    void errorMessageShouldClearAfterSuccessfulLogin() {
        loginPage.login(null, null);
        assertEquals(LoginErrors.USERNAME_REQUIRED, loginPage.errorMessage());

        loginPage.loginAs(TestUsers.STANDARD);
        inventoryPage.waitUntilLoaded();

        assertFalse(loginPage.isErrorMessageDisplayed(),
                "A successful login should not leave the previous error visible");
    }

    @Test
    @Tag("regression")
    void browserBackAfterLogoutShouldNotExposeCatalog() {
        loginPage.loginAs(TestUsers.STANDARD);
        inventoryPage.waitUntilLoaded().logout();

        driver.navigate().back();
        loginPage.waitUntilLoaded();

        assertTrue(loginPage.isLoginButtonDisplayed(),
                "Browser Back after logout should return to the login form");
        assertEquals(TestConfig.BASE_URL, loginPage.currentUrl(),
                "Browser Back after logout must not expose the product catalog");
    }

    
    @Test
    @Tag("regression")
    void protectedPageShouldNotBeAccessibleWithoutLogin() {
        driver.get(TestConfig.BASE_URL + "inventory.html");

        assertEquals(LoginErrors.NOT_LOGGED_IN, loginPage.errorMessage());
        assertEquals(TestConfig.BASE_URL, loginPage.currentUrl());
    }
}