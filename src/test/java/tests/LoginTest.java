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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Verifies the main login-page behaviors using SauceDemo's documented test accounts.
 *
 * <p>The standard user's successful login is the smoke scenario. Account
 * lockout, input validation, password masking, logout, and access control are
 * regression scenarios that can be run separately.
 */
@Tag("login")
class LoginTest extends BaseTest {

    /** Page object for entering credentials and checking login-page feedback. */
    private LoginPage loginPage;

    /** Page object for checking the catalog and logging out after authentication. */
    private InventoryPage inventoryPage;

    /** Opens the login page and prepares page objects before each test. */
    @BeforeEach
    void openLoginPage() {
        loginPage = new LoginPage(driver).open();
        inventoryPage = new InventoryPage(driver);
    }

    /** Confirms that the standard account can sign in and reach the catalog. */
    @Test
    @Tag("smoke")
    void standardUserShouldSeeProductsAfterLogin() {
        loginPage.loginAs(TestUsers.STANDARD);

        inventoryPage.waitUntilLoaded();
        assertEquals("Products", inventoryPage.title());
    }

    /** Confirms that a locked account is rejected and remains on the login page. */
    @Test
    @Tag("regression")
    void lockedOutUserShouldSeeLockedOutMessage() {
        loginPage.loginAs(TestUsers.LOCKED_OUT);

        assertEquals(LoginErrors.LOCKED_OUT, loginPage.errorMessage());
        assertEquals(TestConfig.BASE_URL, loginPage.currentUrl(),
                "A locked-out user must remain on the login page");
    }

    /**
     * Checks that invalid credentials and missing fields produce their expected
     * errors without navigating away from the login page.
     *
     * @param user username to enter, or {@code null} to leave it empty
     * @param pass password to enter, or {@code null} to leave it empty
     * @param expectedError exact error text expected from the application
     */
    @ParameterizedTest(name = "[{index}] {2}")
    @MethodSource("invalidLogins")
    @Tag("regression")
    void invalidLoginShouldShowErrorMessage(String user, String pass, String expectedError) {
        loginPage.login(user, pass);

        assertEquals(expectedError, loginPage.errorMessage());
        assertEquals(TestConfig.BASE_URL, loginPage.currentUrl(),
                "An invalid login must remain on the login page");
    }

    /**
     * Supplies credential combinations and their expected login error messages.
     *
     * @return parameter sets used by {@link #invalidLoginShouldShowErrorMessage(String, String, String)}
     */
    static Stream<Arguments> invalidLogins() {
        return Stream.of(
                arguments("wrong_user", "secret_sauce", LoginErrors.INVALID_CREDENTIALS),
                arguments("standard_user", "wrong_password", LoginErrors.INVALID_CREDENTIALS),
                arguments(null, "secret_sauce", LoginErrors.USERNAME_REQUIRED),
                arguments("standard_user", null, LoginErrors.PASSWORD_REQUIRED));
    }

    /** Confirms that the login password field masks entered characters. */
    @Test
    @Tag("regression")
    void passwordFieldShouldMaskInput() {
        assertEquals("password", loginPage.passwordFieldType());
    }

    /** Confirms that logging out returns the standard user to the login form. */
    @Test
    @Tag("regression")
    void userShouldReturnToLoginPageAfterLogout() {
        loginPage.loginAs(TestUsers.STANDARD);
        inventoryPage.waitUntilLoaded().logout();

        assertTrue(loginPage.isLoginButtonDisplayed(),
                "The login form must be visible after logging out");
        assertEquals(TestConfig.BASE_URL, loginPage.currentUrl());
    }

    /** Confirms that a direct request for the catalog is rejected without login. */
    @Test
    @Tag("regression")
    void protectedPageShouldNotBeAccessibleWithoutLogin() {
        driver.get(TestConfig.BASE_URL + "inventory.html");

        assertEquals(LoginErrors.NOT_LOGGED_IN, loginPage.errorMessage());
        assertEquals(TestConfig.BASE_URL, loginPage.currentUrl());
    }
}