package name.ulbricht.dlx.ui;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings({ "static-method", "checkstyle:AbbreviationAsWordInName" })
@DisplayName("FXML validation")
final class FXMLTest {

    //// Represents the content of an XML file.
    ///
    /// @param imports a set with all imports
    /// @param classes a set with all found class names
    record FileContent(Path file, List<String> imports, Set<String> classes) {

        /// Creates the record and validates the components.

        public FileContent {
            requireNonNull(file);
            requireNonNull(imports);
            requireNonNull(classes);
        }

        @Override
        public String toString() {
            return this.file.getFileName().toString();
        }
    }

    private static List<FileContent> allFiles;

    static synchronized Stream<Arguments> allFiles() throws IOException {
        if (allFiles == null) {
            final var resourcePath = Path.of(".").toAbsolutePath()
                    .resolve("src/main/resources/name/ulbricht/dlx/ui");

            try (var files = Files.find(resourcePath, Integer.MAX_VALUE,
                    (path, _) -> path.toString().endsWith(".fxml"))) {

                allFiles = files.map(FXMLTest::parseFile).toList();
            }
        }

        return allFiles.stream().map(fileContent -> Arguments.argumentSet(fileContent.file().toString(), fileContent));
    }

    /// Pattern for finding the class name in an import statement.
    private static final Pattern IMPORT_CLASS_NAME_PATTERN = Pattern
            .compile("\\s*<\\?import\\s*([A-Za-z0-9\\\\._]+)\\?>");

    /// Pattern for finding class names used as tags.
    private static final Pattern TAG_CLASS_NAME_PATTERN = Pattern.compile("\\s*<(?!/)([A-Z][A-Za-z0-9._]*)\\b[^>]*>");

    /// Pattern for finding class names used as attributes.
    private static final Pattern ATTRIBUTE_CLASS_NAME_PATTERN = Pattern
            .compile("\\s([A-Z][A-Za-z]+)(?:\\.[a-z][A-Za-z0-9_]*)=");

    /// Pattern for extracting the unqualified class name.
    private static final Pattern UNQUALIFIED_CLASS_NAME_PATTERN = Pattern.compile("([a-z]+\\.)*([A-Z][A-Za-z0-9_.]+)");

    private static FileContent parseFile(final Path file) {
        try {
            final var imports = new ArrayList<String>();
            final var classes = new HashSet<String>();

            final var lines = Files.readString(file);

            final var importMatcher = IMPORT_CLASS_NAME_PATTERN.matcher(lines);
            while (importMatcher.find())
                imports.add(importMatcher.group(1));

            final var classMatcher = TAG_CLASS_NAME_PATTERN.matcher(lines);
            while (classMatcher.find())
                classes.add(classMatcher.group(1));

            final var attributeMatcher = ATTRIBUTE_CLASS_NAME_PATTERN.matcher(lines);
            while (attributeMatcher.find())
                classes.add(attributeMatcher.group(1));

            return new FileContent(file, imports, classes);
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @ParameterizedTest
    @MethodSource("allFiles")
    @DisplayName("Missing imports")
    void missingImports(final FileContent content) {
        assertAll(content.classes().stream() //
                .map(className -> //
                () -> assertTrue(
                        content.imports().stream().anyMatch(importName -> importName.endsWith("." + className)),
                        "Missing import for class '%s' in file %s".formatted(className, content.file()))));
    }

    @ParameterizedTest
    @MethodSource("allFiles")
    @DisplayName("Unused imports")
    void unusedImports(final FileContent content) {
        assertAll(content.imports().stream() //
                .map(importName -> {
                    final var matcher = UNQUALIFIED_CLASS_NAME_PATTERN.matcher(importName);
                    assertTrue(matcher.matches());
                    return matcher.group(2);
                }) //
                .map(className -> () -> assertTrue(content.classes().contains(className),
                        "Unused import for class '%s' in file %s".formatted(className, content.file()))));
    }

    @ParameterizedTest
    @MethodSource("allFiles")
    @DisplayName("Duplicate imports")
    void duplicateImports(final FileContent content) {
        assertAll(content.imports().stream() //
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())) //
                .entrySet().stream() //
                .map(entry -> () -> assertEquals(1, entry.getValue().intValue(),
                        "Duplicate import for class '%s' in file %s".formatted(entry.getKey(), content.file()))));
    }

    @ParameterizedTest
    @MethodSource("allFiles")
    @DisplayName("Wrong import order")
    void wrongImportOrder(final FileContent content) {
        final var imports = content.imports();
        if (imports.isEmpty())
            return;

        final var size = imports.size();
        for (var i = 0; i < (size - 1); i++) {
            final var current = imports.get(i);
            final var next = imports.get(i + 1);
            assertTrue(current.compareToIgnoreCase(next) <= 0,
                    () -> "Imports are not correctly sorted: '%s' should be after '%s' in file %s".formatted(current,
                            next, content.file()));
        }
    }

    @ParameterizedTest
    @MethodSource("allFiles")
    @DisplayName("Find classes")
    void findClasses(final FileContent content) {
        final var classLoader = Thread.currentThread().getContextClassLoader();

        assertAll(content.imports().stream() //
                .map(FXMLTest::resourceNameForClassName) //
                .map(resourceName -> () -> {
                    final var resource = classLoader.getResource(resourceName);
                    assertNotNull(resource,
                            "Class resource not found '%s' in file %s".formatted(resourceName, content.file()));
                }));
    }

    private static String resourceNameForClassName(final String className) {
        final var resourceName = new StringBuilder();
        var inner = false;
        for (final var segment : className.split("\\.")) {
            if (!resourceName.isEmpty())
                resourceName.append(inner ? '$' : '/');
            resourceName.append(segment);
            if (Character.isUpperCase(segment.charAt(0)))
                inner = true;
        }
        resourceName.append(".class");
        return resourceName.toString();
    }
}
