package data;

/**
 * Holds the credentials for one SauceDemo account used by automated tests.
 *
 * @param username account name entered in the login form
 * @param password account password entered in the login form
 */
public record TestUser(String username, String password) {}