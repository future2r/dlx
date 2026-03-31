package name.ulbricht.dlx.io;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/// Utility class for reading and writing source files.
public final class SourceFile {

    /// The default file extension for source files.
    public static final String FILE_EXTENSION = ".s";

    /// The alternative file extension for source files.
    public static final List<String> FILE_EXTENSIONS = List.of(FILE_EXTENSION, ".asm");

    /// Reads the content of a source file.
    /// 
    /// @param file the path to the source file
    /// @return the content of the source file
    /// @throws IOException if an I/O error occurs
    public static String read(final Path file) throws IOException {
        requireNonNull(file);

        return Files.readString(file);
    }

    /// Writes content to a source file.
    /// 
    /// @param file    the path to the source file
    /// @param content the content to write
    /// @throws IOException if an I/O error occurs
    public static void write(final Path file, final String content) throws IOException {
        requireNonNull(file);
        requireNonNull(content);

        Files.writeString(file, content);
    }

    /// Private constructor to prevent instantiation.
    private SourceFile() {
    }
}
