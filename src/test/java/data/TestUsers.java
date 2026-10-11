package data;

/**
 * Centralizes the public SauceDemo test accounts so scenarios share consistent
 * and easy-to-maintain credentials.
 */
public final class TestUsers {

    /**
     * This is a public demo credential, not a private secret. In a production
     * project, credentials should be injected through environment variables or
     * a CI secret manager such as GitHub Actions Secrets, never committed to source.
     * A secret is not used here because SauceDemo publishes this shared test
     * password for its sample accounts; storing it in GitHub Secrets would not
     * make the public credential confidential.
     */
    private static final String PASSWORD = "secret_sauce";

    /** Account used by the successful-login smoke scenario. */
    public static final TestUser STANDARD = new TestUser("standard_user", PASSWORD);

    /** Account SauceDemo intentionally prevents from signing in. */
    public static final TestUser LOCKED_OUT = new TestUser("locked_out_user", PASSWORD);

    /** Account whose catalog and product interactions can exhibit defects. */
    public static final TestUser PROBLEM = new TestUser("problem_user", PASSWORD);

    /** Account that can exhibit deliberately slower responses. */
    public static final TestUser PERFORMANCE_GLITCH = new TestUser("performance_glitch_user", PASSWORD);

    /** Account intended to exercise unusual application error behavior. */
    public static final TestUser ERROR = new TestUser("error_user", PASSWORD);

    /** Account intended for visual anomaly investigation. */
    public static final TestUser VISUAL = new TestUser("visual_user", PASSWORD);

    /** Prevents instances of this constants-only utility class. */
    private TestUsers() {}
}