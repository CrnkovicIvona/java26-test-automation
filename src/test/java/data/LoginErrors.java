package data;

/**
 * Exact login and authorization error messages displayed by SauceDemo.
 *
 * <p>Keeping expected text in one place makes assertions consistent and avoids
 * duplicating application messages across test scenarios.
 */
public final class LoginErrors {

    /** Message displayed when the username and password do not match. */
    public static final String INVALID_CREDENTIALS =
            "Epic sadface: Username and password do not match any user in this service";

    /** Message displayed when the username field is empty. */
    public static final String USERNAME_REQUIRED = "Epic sadface: Username is required";

    /** Message displayed when the password field is empty. */
    public static final String PASSWORD_REQUIRED = "Epic sadface: Password is required";

    /** Message displayed when the account is locked out. */
    public static final String LOCKED_OUT = "Epic sadface: Sorry, this user has been locked out.";

    /** Message displayed when a user requests the catalog without authenticating. */
    public static final String NOT_LOGGED_IN =
            "Epic sadface: You can only access '/inventory.html' when you are logged in.";

    /** Prevents instances of this constants-only utility class. */
    private LoginErrors() {}
}
