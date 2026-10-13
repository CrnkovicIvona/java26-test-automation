package reporting;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import reporting.TestResult.Status;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;


public final class SurefireReader {

    
    private static final Pattern RUN_START = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}T\\d{2}-\\d{2}-\\d{2})_\\d+-jvmRun\\d+");
    private static final DateTimeFormatter RUN_START_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");
    private static final Pattern INVOCATION = Pattern.compile("\\[(\\d+)]$");
    private static final List<String> ENVIRONMENT_KEYS = List.of("java.version", "os.name", "headless");
    private static final int MAX_MESSAGE_LENGTH = 300;

    
    private SurefireReader() {}

    
    public static SurefireRun read(Path reportsDir) throws IOException {
        if (!Files.isDirectory(reportsDir)) {
            throw new IOException("Surefire report directory not found: " + reportsDir
                    + ". Run the tests first, for example .\\mvnw.cmd test");
        }
        List<Path> files;
        try (var stream = Files.list(reportsDir)) {
            files = stream
                    .filter(path -> path.getFileName().toString().startsWith("TEST-"))
                    .filter(path -> path.getFileName().toString().endsWith(".xml"))
                    .sorted()
                    .toList();
        }
        if (files.isEmpty()) {
            throw new IOException("No TEST-*.xml files found in " + reportsDir + ". Run the tests first.");
        }

        var results = new ArrayList<TestResult>();
        var started = new TreeSet<LocalDateTime>();
        var environment = new TreeMap<String, SortedSet<String>>();
        for (var file : files) {
            parse(file, results, started, environment);
        }
        return new SurefireRun(List.copyOf(results), started, environment);
    }

    
    private static void parse(Path file, List<TestResult> results, SortedSet<LocalDateTime> started,
                              TreeMap<String, SortedSet<String>> environment) throws IOException {
        Document document;
        try (var input = Files.newInputStream(file)) {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            document = factory.newDocumentBuilder().parse(input);
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Cannot read " + file + ": " + e.getMessage(), e);
        }

        var suite = document.getDocumentElement();
        readProperties(suite, started, environment);

        NodeList testCases = suite.getElementsByTagName("testcase");
        for (var i = 0; i < testCases.getLength(); i++) {
            results.add(toResult((Element) testCases.item(i)));
        }
    }

    
    private static void readProperties(Element suite, SortedSet<LocalDateTime> started,
                                       TreeMap<String, SortedSet<String>> environment) {
        NodeList properties = suite.getElementsByTagName("property");
        for (var i = 0; i < properties.getLength(); i++) {
            var property = (Element) properties.item(i);
            var name = property.getAttribute("name");
            var value = property.getAttribute("value");
            if (ENVIRONMENT_KEYS.contains(name)) {
                environment.computeIfAbsent(name, key -> new TreeSet<>()).add(value);
            } else if (name.equals("sun.java.command")) {
                var matcher = RUN_START.matcher(value);
                if (matcher.find()) {
                    try {
                        started.add(LocalDateTime.parse(matcher.group(1), RUN_START_FORMAT));
                    } catch (DateTimeParseException ignored) {
                        
                    }
                }
            }
        }
    }

    
    private static TestResult toResult(Element testCase) {
        var name = testCase.getAttribute("name");
        var parenthesis = name.indexOf('(');
        var method = parenthesis >= 0 ? name.substring(0, parenthesis) : INVOCATION.matcher(name).replaceFirst("");
        var invocationMatcher = INVOCATION.matcher(name);
        Integer invocation = invocationMatcher.find() ? Integer.valueOf(invocationMatcher.group(1)) : null;

        var status = Status.PASSED;
        var message = "";
        var children = testCase.getChildNodes();
        for (var i = 0; i < children.getLength(); i++) {
            if (!(children.item(i) instanceof Element child)) {
                continue;
            }
            switch (child.getTagName()) {
                case "failure", "error" -> {
                    status = Status.FAILED;
                    message = shorten(child.getAttribute("message"));
                }
                case "skipped" -> {
                    if (status != Status.FAILED) {
                        status = Status.SKIPPED;
                        message = shorten(child.getAttribute("message"));
                    }
                }
                default -> { }
            }
        }
        return new TestResult(testCase.getAttribute("classname"), method, name, invocation,
                status, parseSeconds(testCase.getAttribute("time")), message);
    }

    
    private static double parseSeconds(String text) {
        if (text.isBlank()) {
            return 0;
        }
        return Double.parseDouble(text.replace(",", ""));
    }

    
    private static String shorten(String message) {
        var compact = message.replaceAll("\\s+", " ").trim();
        return compact.length() <= MAX_MESSAGE_LENGTH ? compact : compact.substring(0, MAX_MESSAGE_LENGTH) + "...";
    }
}
