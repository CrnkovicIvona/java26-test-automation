package data;

import org.junit.jupiter.params.provider.Arguments;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public final class CheckoutTestData {

    private CheckoutTestData() {}

    public static CustomerInformation randomCustomer() {
        var uniqueId = UUID.randomUUID().toString().replace("-", "");
        var postalCode = String.valueOf(ThreadLocalRandom.current().nextInt(10_000, 100_000));

        return new CustomerInformation(
                "TestFirst" + uniqueId.substring(0, 8),
                "TestLast" + uniqueId.substring(8, 16),
                postalCode);
    }

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

    public record CustomerInformation(String firstName, String lastName, String postalCode) {}
}