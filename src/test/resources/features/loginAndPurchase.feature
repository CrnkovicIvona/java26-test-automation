Feature: Login and purchase flow for SauceDemo
  As a SauceDemo user
  I want to sign in, browse products, and complete checkout
  So that I can buy items from the store

  Scenario: Standard user can log in and view products
    Given the user is on the SauceDemo login page
    When the user logs in with standard credentials
    Then the products page should be displayed

  Scenario: Locked user cannot log in
    Given the user is on the SauceDemo login page
    When the user logs in with locked out credentials
    Then the locked out error message should be displayed

  Scenario: Unknown user is rejected
    Given the user is on the SauceDemo login page
    When the user logs in with "wrong_user" and "secret_sauce"
    Then the validation error should be "Epic sadface: Username and password do not match any user in this service"

  Scenario: Wrong password is rejected
    Given the user is on the SauceDemo login page
    When the user logs in with "standard_user" and "wrong_password"
    Then the validation error should be "Epic sadface: Username and password do not match any user in this service"

  Scenario: Empty username is rejected
    Given the user is on the SauceDemo login page
    When the user logs in with "" and "secret_sauce"
    Then the validation error should be "Epic sadface: Username is required"

  Scenario: Empty password is rejected
    Given the user is on the SauceDemo login page
    When the user logs in with "standard_user" and ""
    Then the validation error should be "Epic sadface: Password is required"

  Scenario: Empty username and password are rejected
    Given the user is on the SauceDemo login page
    When the user logs in with "" and ""
    Then the validation error should be "Epic sadface: Username is required"

  Scenario: User can add a product to the cart
    Given the user is logged in as a standard user
    When the user adds the "Sauce Labs Backpack" to the cart
    Then the cart should show 1 item
    And the cart page should display the product "Sauce Labs Backpack"

  Scenario: User can complete a valid checkout
    Given the user is logged in as a standard user
    And the user adds the "Sauce Labs Backpack" to the cart
    When the user proceeds to checkout and submits valid customer information
    Then the checkout overview should be displayed
    When the user finishes checkout
    Then the order confirmation page should be displayed

  Scenario: User can log out and return to the login form
    Given the user is logged in as a standard user
    When the user logs out from the inventory page
    Then the login form should be visible again
