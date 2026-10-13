package data;

public final class TestUsers {

    private static final String PASSWORD = "secret_sauce";

    public static final TestUser STANDARD = new TestUser("standard_user", PASSWORD);

    public static final TestUser LOCKED_OUT = new TestUser("locked_out_user", PASSWORD);

    public static final TestUser PROBLEM = new TestUser("problem_user", PASSWORD);

    public static final TestUser PERFORMANCE_GLITCH = new TestUser("performance_glitch_user", PASSWORD);

    public static final TestUser ERROR = new TestUser("error_user", PASSWORD);

    public static final TestUser VISUAL = new TestUser("visual_user", PASSWORD);

    private TestUsers() {}
}