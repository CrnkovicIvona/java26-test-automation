package steps;

import config.TestConfig;
import data.LoginErrors;
import data.TestUsers;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.WebDriver;
import pages.CartPage;
import pages.CheckoutCompletePage;
import pages.CheckoutStepOnePage;
import pages.CheckoutStepTwoPage;
import pages.InventoryPage;
import pages.LoginPage;
import support.DriverFactory;
import support.ScreenshotSupport;

import java.io.IOException;

public class LoginStepDefinitions {

    private WebDriver driver;
    private LoginPage loginPage;
    private InventoryPage inventoryPage;
    private CartPage cartPage;
    private CheckoutStepOnePage checkoutStepOnePage;
    private CheckoutStepTwoPage checkoutStepTwoPage;
    private CheckoutCompletePage checkoutCompletePage;

    @Before
    public void setUp() {
        driver = DriverFactory.create();
        loginPage = new LoginPage(driver);
        inventoryPage = new InventoryPage(driver);
    }

    @After
    public void tearDown(Scenario scenario) throws IOException {
        try {
            if (driver != null && scenario.isFailed()) {
                scenario.attach(ScreenshotSupport.capture(driver, "Cucumber_" + scenario.getName()),
                        "image/png", scenario.getName());
            }
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    @Given("the user is on the SauceDemo login page")
    public void userIsOnLoginPage() {
        loginPage.open();
    }

    @Given("the user is logged in as a standard user")
    public void userIsLoggedInAsStandardUser() {
        loginPage.open();
        loginPage.loginAs(TestUsers.STANDARD);
        inventoryPage = new InventoryPage(driver).waitUntilLoaded();
    }

    @When("the user logs in with standard credentials")
    public void userLogsInWithStandardCredentials() {
        loginPage.loginAs(TestUsers.STANDARD);
    }

    @When("the user logs in with locked out credentials")
    public void userLogsInWithLockedOutCredentials() {
        loginPage.loginAs(TestUsers.LOCKED_OUT);
    }

    @When("the user logs in with {string} and {string}")
    public void userLogsInWithCredentials(String username, String password) {
        loginPage.login(username, password);
    }

    @When("the user adds the {string} to the cart")
    public void userAddsProductToCart(String productName) {
        inventoryPage = new InventoryPage(driver).waitUntilLoaded();
        inventoryPage.addProductToCart(productName);
        cartPage = inventoryPage.openCart();
    }

    @When("the user proceeds to checkout and submits valid customer information")
    public void userProceedsToCheckout() {
        cartPage = inventoryPage.openCart();
        checkoutStepOnePage = cartPage.proceedToCheckout();
        checkoutStepOnePage.enterFirstName("Jane");
        checkoutStepOnePage.enterLastName("Doe");
        checkoutStepOnePage.enterPostalCode("10001");
        checkoutStepTwoPage = checkoutStepOnePage.continueCheckout();
    }

    @When("the user finishes checkout")
    public void userFinishesCheckout() {
        checkoutCompletePage = checkoutStepTwoPage.finishCheckout();
    }

    @When("the user logs out from the inventory page")
    public void userLogsOutFromInventoryPage() {
        inventoryPage = new InventoryPage(driver).waitUntilLoaded();
        inventoryPage.logout();
    }

    @Then("the products page should be displayed")
    public void productsPageShouldBeDisplayed() {
        inventoryPage = new InventoryPage(driver).waitUntilLoaded();
        Assertions.assertEquals("Products", inventoryPage.title());
    }

    @Then("the locked out error message should be displayed")
    public void lockedOutErrorMessageShouldBeDisplayed() {
        Assertions.assertEquals(LoginErrors.LOCKED_OUT, loginPage.errorMessage());
        Assertions.assertEquals(TestConfig.BASE_URL, loginPage.currentUrl());
    }

    @Then("the validation error should be {string}")
    public void validationErrorShouldBe(String expectedMessage) {
        Assertions.assertEquals(expectedMessage, loginPage.errorMessage());
        Assertions.assertEquals(TestConfig.BASE_URL, loginPage.currentUrl());
    }

    @Then("the cart should show {int} item")
    public void cartShouldShowItemCount(int expectedCount) {
        cartPage = inventoryPage.openCart();
        Assertions.assertEquals(expectedCount, cartPage.itemCount());
        Assertions.assertEquals(expectedCount, cartPage.badgeCount());
    }

    @Then("the cart page should display the product {string}")
    public void cartPageShouldDisplayProduct(String productName) {
        Assertions.assertTrue(cartPage.items().stream()
                .anyMatch(item -> item.name().equals(productName)));
    }

    @Then("the checkout overview should be displayed")
    public void checkoutOverviewShouldBeDisplayed() {
        Assertions.assertNotNull(checkoutStepTwoPage);
        Assertions.assertEquals("Checkout: Overview", checkoutStepTwoPage.title());
    }

    @Then("the order confirmation page should be displayed")
    public void orderConfirmationPageShouldBeDisplayed() {
        Assertions.assertNotNull(checkoutCompletePage);
        Assertions.assertEquals("Checkout: Complete!", checkoutCompletePage.title());
        Assertions.assertEquals("Thank you for your order!", checkoutCompletePage.confirmationHeading());
    }

    @Then("the login form should be visible again")
    public void loginFormShouldBeVisibleAgain() {
        Assertions.assertTrue(loginPage.isLoginButtonDisplayed());
        Assertions.assertEquals(TestConfig.BASE_URL, loginPage.currentUrl());
    }

}
