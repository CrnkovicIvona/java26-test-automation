package support;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public final class ScreenshotSupport {

    private ScreenshotSupport() {}

    public static byte[] capture(WebDriver driver, String name) throws IOException {
        var screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        var safeName = name.replaceAll("[^A-Za-z0-9._-]", "_");
        var file = Path.of("target", "screenshots",
                safeName + "_" + UUID.randomUUID() + ".png");
        Files.createDirectories(file.getParent());
        Files.write(file, screenshot);
        return screenshot;
    }
}
