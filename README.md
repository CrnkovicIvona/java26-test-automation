# java26-test-automation
End-to-end test automation project for SauceDemo using Java 26, Selenium WebDriver, JUnit 5 and Maven. Demonstrates Page Object Model, data-driven testing, CI-ready design and QA automation best practices.


# Rules for Writing High-Quality Automated Tests for SauceDemo

Approaching the project `https://www.saucedemo.com/` goal is not merely to write tests that pass, but to build reliable, readable, maintainable and meaningful automation that verifies the application's behavior from the user's perspective.
Focus on three things: testing business rules, test independence and reliable assertions. These are the foundations that distinguish automation that merely demonstrates a tool from automation that a team can trust.

SauceDemo is an excellent project for practice because it covers the entire basic e-commerce flow: login, product catalog, sorting, cart and checkout. An additional advantage is that it offers user accounts with different behaviors, which makes it possible to test positive, negative and edge-case scenarios.

Below are the rules I would follow when writing tests, regardless of whether to use Playwright with TypeScript, Selenium with Java, or some other tool.

## 1. Fundamental Rules of QA Automation

**1. Every test must have a clear purpose**

Before writing code, define what you are verifying, why it matters and what result you expect. Do not automate a scenario just because it can be automated.

**2. Use clear assertions**

It is not enough to click a button and check that no error occurred. Verify the concrete business outcome, for example that the product has been added to the cart and that the item count has been updated.

**3. Test at the appropriate level**

Use UI automation to verify user flows and interface behavior. Do not test every detail of the application through the entire UI flow if it can be verified more simply and reliably at another level.

**4. Tests must be independent**

Each test should be able to run on its own, without relying on state left behind by a previous test. Do not assume the cart is empty just because the previous test finished.

**5. Tests must be repeatable**

The same test, with the same preconditions, should always give the same result. An unstable test that sometimes passes and sometimes fails without any real change in the application is a flaky test, and its cause must be found.

**6. Write maintainable code**

If the way a button is located or the page structure changes, the change should require as few edits as possible. Extract repeated logic into functions, helper methods or Page Object models where it makes sense.

## 2. Rules for Organizing Tests

Define the structure of the test suite before writing code.

- One test, one main responsibility. If a test fails, it should be clear which part of the functionality is broken.
- Test names must describe behavior. Example: `should_remove_product_from_cart`, not `test_03`.
- Arrange – Act – Assert. Prepare the preconditions, perform the action, verify the result.
- Setup and teardown. Handle login, navigation and state cleanup at an appropriate level, without unnecessary repetition.
- Do not depend on execution order. The cart test must not require the catalog test to have completed successfully beforehand.
- Separate test data from logic. Usernames, expected products and checkout data should be centralized or clearly defined.
- Use a parameterized/data-driven approach when the same logic is verified with several different inputs.
- Do not over-abstract. A Page Object or helper should reduce duplication, not hide the entire flow so that it is hard to understand what the test does.
- Tag tests by purpose. For example `smoke`, `regression`, `login`, `cart`, `checkout`.
- Separate tests that verify known issues or intentionally faulty behavior from the standard happy-path smoke suite.

Important: you do not need to build a large framework right away. For a small project, the best one is structured enough to be maintainable but not unnecessarily complicated.

## 3. Rules for Locators and Element Interaction

A locator is the way a test finds an element on the page. A bad locator is one of the common causes of brittle automated tests.

Recommended order of choice:

1. Dedicated test attributes such as `data-test="login-button"`, if they exist.
2. Semantic locators based on role and accessible name, for example the "Login" button.
3. Stable IDs, for example `#user-name`.
4. CSS selectors based on a stable structure.
5. XPath when there is a justified reason.

In the SauceDemo application, many elements have dedicated `data-test` attributes, which is useful for automation.

Rules to follow:

- Avoid long XPath expressions that depend on the depth of the DOM structure.
- Avoid locating by an element's ordinal number, for example "the third button on the page", unless the order is exactly what you are testing.
- Do not use CSS classes meant for styling as the only criterion if a more stable locator exists.
- When there are several identical "Add to cart" buttons, locate the button within the corresponding product card.
- Verify that the locator finds the expected number of elements.
- Do not randomly add waits or repeat clicks when a locator does not work. First determine the cause.
- Do not use JavaScript for forced clicks as a routine solution for an element that cannot be clicked normally.
- For dynamic elements, use the tool's automatic waiting and checks for visibility, availability and ability to interact.

Example of a good principle: if you are testing adding the product "Sauce Labs Backpack", find that product's card, and then within it the button for adding. This way the test stays tied to business meaning rather than to the position of the button on the page.

## 4. Rules for Waiting and Synchronization

One of the most important rules: do not use fixed pauses as a substitute for waiting on a concrete condition.

Bad approach:

```
await page.waitForTimeout(3000);
```

Better approach in Playwright:

```
await expect(page).toHaveURL(/inventory\.html/);
await expect(page.getByText('Products')).toBeVisible();
```

Adhere to the following:

- Wait for the expected state, not merely for time to pass.
- Use the automatic waits that the framework provides.
- Set reasonable timeouts for the whole test and for individual operations.
- Do not increase a timeout just to mask a slow or unstable test.
- With a slow user, verify that the functionality is nevertheless correct, without assuming the response will arrive within a fixed number of milliseconds.
- Distinguish waiting for a page to load from waiting for a business result.
- Do not rely exclusively on `networkidle`, because the application may have network activity that is not relevant to the completion of the user action.

## 5. Rules for Assertions and Verifying Results

An assertion must confirm what the user or the business process expects, not merely that the action was performed.

| Functionality | What to verify |
| --- | --- |
| Login | URL, catalog display, absence of an error message |
| Invalid login | Error message, staying on the login page |
| Catalog | Products, names, prices and descriptions |
| Sorting | The actual order of the data, not just the selected option |
| Cart | Product names, item count, prices and removal |
| Checkout | Field validation, order contents and amounts |
| Order completion | Success confirmation and the expected final state |

Additional rules:

- Assertions should be precise enough to reveal the error.
- Do not check only that an element is visible if its content needs to be confirmed.
- Do not write your own conditions that silently pass when the expected element does not exist.
- Verify both positive and negative states.
- For amounts, use precise verification of numerical values and the expected calculation, instead of vague matching of part of the text.
- Do not overdo assertions that are unrelated to the goal of the test.

## 6. Rules for Test Data and User Accounts

For SauceDemo, use the different accounts deliberately, because they do not all represent the same scenario.

| User account | Testing purpose |
| --- | --- |
| `standard_user` | Standard positive flows and regression |
| `locked_out_user` | Rejection of a locked-out user's login |
| `problem_user` | Investigating and verifying anomalies in the interface and behavior |
| `performance_glitch_user` | Verifying functionality with slowed responses |
| `error_user` | Investigating behavior related to errors |
| `visual_user` | Visual checks and display comparison |

The shared password for these accounts is `secret_sauce`, according to the information for the test application.

Rules for test data:

- Use `standard_user` for the basic happy path.
- `locked_out_user` is expected to be rejected at login; this is not an unexpected application failure.
- Do not assume that all accounts will behave identically after login.
- For `problem_user`, `error_user` and `visual_user`, do not automatically expect the same result as for the standard user. First document the observed behavior and define the expectations.
- If an account's behavior is intentionally faulty, separate the confirmation of the known issue from the standard user's regression tests.
- Use data that is deterministic and simple to verify.
- Do not use personal or real customer data.
- Test data should be easy to change without editing dozens of tests.

## 7. Rules for Testing Login and Authentication

Login is a good candidate for a separate smoke test because it is a prerequisite for most of the application's functionality.

### Recommended Scenarios

| ID | Scenario | Input | Expectation |
| --- | --- | --- | --- |
| LOGIN-001 | Valid credentials | standard_user + secret_sauce | The user lands on the product catalog. |
| LOGIN-002 | Wrong password | standard_user + wrong password | Login is rejected and a message is displayed. |
| LOGIN-003 | Non-existent user | Unknown username | Login is rejected. |
| LOGIN-004 | Empty fields | Username and password empty | An appropriate validation message is displayed. |
| LOGIN-005 | Empty password | Valid username, empty password | Login is rejected. |
| LOGIN-006 | Locked-out user | locked_out_user + secret_sauce | A message about the locked account is displayed. |
| LOGIN-007 | Logout | Logged-in standard_user | The user returns to the login page and the protected page is not accessible without a session. |

A few more rules:

- Do not rely only on the URL change. Also verify that the catalog has actually loaded.
- Verify that the error message disappears after a successful login or is updated correctly after a new attempt.
- Verify that the user can log out from the menu.
- Verify the behavior of the Back button after logout and direct access to protected routes.
- Verify that the password in the field is not displayed as plain text.
- Distinguish functional form validation from testing real authentication security. Since this is a demo application, do not treat its behavior as proof of the security of a production system.

## 8. Rules for Testing the Product Catalog

Do not test the catalog only by checking that the page opened. Verify content and behavior.

**Product display**

- Verify that the expected products are displayed.
- Verify names, descriptions and prices.
- Verify that images are loaded and match the products.
- Verify that each product has the appropriate action.
- Verify that product details match the details in the catalog.

**Sorting**

Test all four available options:

| Option | Expected order |
| --- | --- |
| Name (A to Z) | Names ascending |
| Name (Z to A) | Names descending |
| Price (low to high) | Prices ascending |
| Price (high to low) | Prices descending |

Rule: verify the actual sequence of values after sorting. It is not enough to confirm that the dropdown selected `Price (low to high)`.

Additionally verify:

- Sorting products with equal prices.
- Opening product details and returning to the catalog.
- Preservation or resetting of the sort order after navigation, according to a predefined expectation.
- The behavior of all accounts that have special anomalies.

## 9. Rules for Testing the Cart

The cart must be verified as a set of related states, not just a set of clicks.

**Adding one product**

The cart counter shows 1, and the cart contains the correct product.

**Adding multiple products**

The counter and the list match the number of added products.

**Removing a product**

The product disappears and the counter is updated.

**Removing all products**

The cart is empty and does not show stale items.

**Navigation between pages**

The cart state remains consistent when going to the catalog and returning.

**Add to cart / Remove button**

The button text and the cart state are consistent with each other.

**Continuing shopping**

Returning to the catalog allows the process to continue without an unexpected loss of state.

Take particular care that a test does not assume an empty cart without that state having been ensured beforehand. Also verify that the cart contains exactly the expected products, not just the correct number of items.

## 10. Rules for Testing the Checkout Process

Checkout is the most important end-to-end flow because it connects several functionalities into a single user action.

**1. Cart**

Products, quantities, prices

**2. Your Information**

First name, last name, postal code

**3. Overview**

Order summary and calculation of amounts

**4. Complete**

Confirmation of the successfully completed order

### Scenarios to Cover

- Successful checkout with one product.
- Successful checkout with multiple products.
- Empty first name.
- Empty last name.
- Empty postal code.
- All required fields filled in correctly.
- Returning from the order overview to the catalog or cart.
- Verifying the order contents on the final overview.
- Verifying the subtotal amount.
- Verifying the tax and the total amount.
- Completing the order and displaying the confirmation.
- Verifying that the expected cart state is shown after the order is completed.

Key rule: if the cart contains products priced at 29.99 and 9.99, the expected subtotal is 39.98. For the total amount, apply the tax rule that the application displays. Do not claim in advance that the calculation is correct just because some value is visible; calculate the expected value independently and compare it with the application.

## 11. Rules for Negative, Edge and Exploratory Tests

Besides the usual user flows, also verify situations in which the user does not do everything in the expected order.

- Try to continue checkout with incomplete data.
- Try to open checkout without products.
- Try to navigate directly to the URL of a checkout stage.
- Try to reload the page during the process.
- Verify behavior after logout.
- Verify what happens if an action is repeated.
- Verify keyboard navigation.
- Verify responsiveness at different screen sizes.
- Verify the visibility and clarity of error messages.
- Verify the behavior of special user accounts, but distinguish expected anomalies from new regressions.

Do not put every possible edge case into the smoke test. A smoke test should quickly confirm that the most important functionalities are available. Broader negative and regression tests can be run separately.

## 12. Rules for the Code Quality of Automated Tests

These are the rules I would expect in a serious QA automation project, even when working on a demonstration application.

| Rule | What it means in practice |
| --- | --- |
| Readability | A test can be understood without studying every helper method. |
| DRY | You do not unnecessarily duplicate the same logic. |
| Single responsibility | Each test has a clear primary goal. |
| Explicit expectations | The expected result is not hidden in complex logic. |
| Minimal complexity | You do not build a framework larger than the project itself. |
| Typing | In TypeScript, define types for test data and avoid `any` without justification. |
| Centralized configuration | URL, timeouts and test data are not scattered across the whole project. |
| No hardcoded pauses | Waits are based on the application state. |
| No hidden dependencies | A test does not require a previous test or a manually prepared browser. |
| Useful error messages | A test failure should explain what was expected and what was found. |

### Example of a Good Project Structure

Suggested:

```
saucedemo-tests/
├── tests/
│   ├── login.spec.ts
│   ├── inventory.spec.ts
│   ├── cart.spec.ts
│   └── checkout.spec.ts
├── pages/
│   ├── LoginPage.ts
│   ├── InventoryPage.ts
│   ├── CartPage.ts
│   └── CheckoutPage.ts
├── fixtures/
│   └── test-fixtures.ts
├── test-data/
│   └── users.ts
├── playwright.config.ts
├── package.json
└── README.md
```

This is an initial recommendation, not a mandatory structure. For a few tests you can start more simply. Introduce Page Object models when they genuinely make maintenance easier.

## 13. Rules for Stability and Diagnostics

An automated test is not high quality just because it passes on your computer.

- Test in both headless and headed mode.
- Run tests multiple times to detect instability.
- Use isolated browser contexts or the framework's appropriate isolation mechanism.
- Set a controlled retry policy for failed tests. A retry is not a substitute for fixing the cause.
- Save a screenshot, trace or video when a test fails, where it makes sense.
- Record the expected and actual state.
- Distinguish an application defect, a test code defect and an infrastructure problem.
- Before reporting a bug, try to reproduce the problem outside the automated test.
- Do not automatically mark as passed a test that failed and then passed on retry. Track it as unstable.
- Adjust timeouts to the observed behavior of the application and the CI environment.

With `performance_glitch_user`, it is especially important that the test does not fail merely because the page is slower than with the standard user. On the other hand, do not wait indefinitely or remove performance checks if they are part of the requirements.

## 14. Rules for Accessibility, Visual Testing and Security

These areas are often neglected when automation is reduced to just clicks.

**Accessibility**

- Navigating through the form and controls using the keyboard.
- Logical focus order.
- Visible focus of the active element.
- Accessible names of fields and buttons.
- Understandable error messages.
- Basic checks of contrast and semantic structure.

**Visual testing**

- Verify that images and text are displayed correctly.
- Pay attention to displaced elements, overlaps and faulty displays.
- For visual comparisons, use controlled viewports, browser and test data.
- Do not treat every pixel difference as a real bug; define tolerances for visual tests.
- `visual_user` can be useful for investigating anomalies, but first document what is expected from its behavior.

**Security**

- Verify that protected content is not displayed after logout.
- Do not store secrets or real user data in the repository.
- Do not print passwords and tokens in logs.
- Testing for XSS, injections and authentication bypass should be carried out in a controlled manner and within the agreed scope.
- Do not conclude from the results of a UI test that the backend is secure.

## 15. Rules for Git, CI/CD and Reporting

A quality automation project should be runnable outside the local computer as well.

- Keep the code in a Git repository.
- Use meaningful names for branches, commits and pull requests.
- Run smoke tests on pull requests.
- Run the broader regression suite as needed, for example after a merge or on a schedule.
- Include the installation of dependencies and required browsers in the CI configuration.
- Do not commit generated results, secrets or unnecessary temporary files to the repository.
- Save test reports and artifacts when a test fails.
- Clearly display the total number of passed, failed and skipped tests.
- Document how the project is installed, run and maintained.
- Ensure that the tests can be run with a single command according to the instructions in the README.

For a small portfolio project, it is enough to show a reliable test suite, an understandable structure and automated execution on GitHub Actions. There is no need to immediately introduce a complex orchestration system.

## 16. Priorities: What to Automate First?

You do not have to write all scenarios at once. Start with the most important functionalities, and then expand coverage.

**P1 — Smoke tests**

- Successful login of the standard user.
- Catalog loading.
- Adding a product to the cart.
- Basic successful checkout.
- Order completion confirmation.

**P2 — Regression tests**

- Invalid login credentials.
- Locked-out user.
- All sorting options.
- Adding and removing multiple products.
- Validation of required checkout fields.
- Verification of prices, tax and total amount.

**P3 — Additional checks**

- Visual anomalies and special user accounts.
- Accessibility.
- Different viewports and browsers.
- Navigation after logout.
- Instability, slower responses and diagnostics.

Priority should be determined by the risk and importance of the functionality, not by how easy a test is to write.

## 17. Final Checklist for Every Test

Before you accept an automated test as done, go through this list.

### Test review checklist

- [ ] The test has a clear goal and describes the expected behavior.
- [ ] Preconditions and test data are defined.
- [ ] The test works on its own and does not depend on execution order.
- [ ] It uses stable locators.
- [ ] It does not use fixed pauses as the main way of waiting.
- [ ] It verifies the actual result using assertions.
- [ ] It covers the appropriate positive or negative scenario.
- [ ] It can be repeated without manually resetting the application.
- [ ] The failure message makes diagnosis easier.
- [ ] There is no unnecessary duplication or excessive abstraction.
- [ ] The test has been run multiple times and its stability has been verified.
- [ ] The result is visible in the report and, where necessary, accompanied by evidence.

## Running the tests

### Prerequisites

- Java 26 (JDK). The Maven Wrapper downloads and runs the project's pinned Maven version.
- Google Chrome or Chromium for local runs. Selenium Manager resolves the matching driver automatically.
- An internet connection and access to `https://www.saucedemo.com/`.

### Commands

Run the full test suite:

```powershell
.\mvnw.cmd test
```

On macOS or Linux, use `./mvnw` in place of `.\mvnw.cmd`.

Run headless, as in CI:

```powershell
.\mvnw.cmd -Dheadless=true test
```

Run only smoke tests:

```powershell
.\mvnw.cmd -Dheadless=true -Dgroups=smoke test
```

The smoke suite includes checks that all six catalog products display a loaded image, name, description, price, and enabled cart button; all four sorting options produce the expected product order; product details match the catalog and return to it correctly; adding a product updates the cart count and button state; cart rows show the selected product; the checkout overview displays correct line items and totals; and a completed order shows its confirmation.

Run only the focused checkout and product-page tests:

```powershell
.\mvnw.cmd -Dheadless=true -Dgroups="product-details,cart,checkout-step-one,checkout-step-two,checkout-complete" test
```

Run the same focused tests with a visible Chrome window by omitting `-Dheadless=true`.

Run the separately tagged special-user scenarios:

```powershell
.\mvnw.cmd -Dheadless=true -Dgroups=special-user test
```

Checkout regression tests cover empty carts and orders, required-field validation, accessible field names and keyboard focus, cart removal and navigation, and subtotal/tax/total calculations. Each test creates a fresh browser session; no automatic retry is configured, so a failed test remains failed and retains its failure evidence.

GitHub Actions runs smoke tests on pushes and pull requests targeting `main` or `master`; they can also be started manually with **Run workflow**. The JUnit report contains pass/fail/skip counts and failure details in `target/surefire-reports/`. Failed tests capture screenshots in `target/screenshots/`. Both directories are uploaded as workflow artifacts when available, even when tests fail.

## Special-user scenarios and known limitations

`ProblematicUserTest` keeps the SauceDemo `problem_user`, `performance_glitch_user`, `error_user` and `visual_user` scenarios separate from the normal smoke suite. Each scenario checks that the account can reach the catalog; these tests are a starting point for investigating each account's intentionally unusual behavior and do not assert specific visual or product-interaction defects.

The CI workflow depends on the availability and current behavior of the public SauceDemo site and a compatible Chrome browser on the GitHub-hosted runner. Special-user behavior can change upstream, and a successful catalog check does not mean that all account-specific defects have been validated. Smoke tests intentionally cover only the standard user's successful login.
