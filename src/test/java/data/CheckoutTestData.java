package data;

import org.junit.jupiter.params.provider.Arguments;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

/**
 * Centralizes customer information and validation scenarios used by checkout tests.
 *
 * <p>This class contains test values only; browser interactions remain in the
 * checkout page objects and assertions remain in the test classes.
 */
public final class CheckoutTestData {

    /** Prevents instances of this constants-and-providers utility class. */
    private CheckoutTestData() {}

    /**
     * Generates new, non-empty synthetic customer details for a checkout run.
     *
     * <p>SauceDemo only requires these fields to be non-empty, so unique
     * synthetic names and a five-digit postal code are sufficient without an
     * external test-data dependency.
     *
     * @return newly generated customer information
     */
    public static CustomerInformation randomCustomer() {
        var uniqueId = UUID.randomUUID().toString().replace("-", "");
        var postalCode = String.valueOf(ThreadLocalRandom.current().nextInt(10_000, 100_000));

        return new CustomerInformation(
                "TestFirst" + uniqueId.substring(0, 8),
                "TestLast" + uniqueId.substring(8, 16),
                postalCode);
    }

    /**
     * Supplies one invalid submission for each required checkout customer field.
     *
     * <p>Each case leaves exactly one field empty so the expected validation
     * message identifies the field under test.
     *
     * @return parameter sets containing a field label, expected error, and submitted values
     */
    public static Stream<Arguments> missingCustomerFields() {
        var missingFirstNameCase = randomCustomer();
        var missingLastNameCase = randomCustomer();
        var missingPostalCodeCase = randomCustomer();

        return Stream.of(
                Arguments.of("first name", "Error: First Name is required",
                        "", missingFirstNameCase.lastName(), missingFirstNameCase.postalCode()),
                Arguments.of("last name", "Error: Last Name is required",
                        missingLastNameCase.firstName(), "", missingLastNameCase.postalCode()),
                Arguments.of("postal code", "Error: Postal Code is required",
                        missingPostalCodeCase.firstName(), missingPostalCodeCase.lastName(), ""));
    }

    /**
     * Holds the customer values submitted to the checkout information form.
     *
     * @param firstName customer's first name
     * @param lastName customer's last name
     * @param postalCode customer's postal or ZIP code
     */
    public record CustomerInformation(String firstName, String lastName, String postalCode) {}
}
