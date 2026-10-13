package data;

public final class LoginErrors {

    public static final String INVALID_CREDENTIALS =
            "Epic sadface: Username and password do not match any user in this service";

    public static final String USERNAME_REQUIRED = "Epic sadface: Username is required";

    public static final String PASSWORD_REQUIRED = "Epic sadface: Password is required";

    public static final String LOCKED_OUT = "Epic sadface: Sorry, this user has been locked out.";

    public static final String NOT_LOGGED_IN =
            "Epic sadface: You can only access '/inventory.html' when you are logged in.";

    private LoginErrors() {}
}
