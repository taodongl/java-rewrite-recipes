package io.github.taodongl;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import com.google.errorprone.refaster.annotation.Repeated;
import org.openrewrite.java.template.RecipeDescriptor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

/**
 * Refaster templates implementing IntelliJ's {@code ReadWriteStringCanBeUsed} inspection:
 * modernize byte-based {@link java.nio.file.Files} read/write idioms to the
 * {@link java.nio.file.Files#readString} and {@link java.nio.file.Files#writeString}
 * methods introduced in Java 11.
 *
 * @see <a href="https://www.jetbrains.com/help/inspectopedia/ReadWriteStringCanBeUsed.html">ReadWriteStringCanBeUsed</a>
 */
@RecipeDescriptor(
        name = "`Files.readString()` or `Files.writeString()` can be used",
        description = "Replace byte-based `Files` read/write idioms with the `Files.readString()` and " +
                      "`Files.writeString()` methods introduced in Java 11."
)
public class ReadWriteStringCanBeUsed {

    /**
     * {@code new String(Files.readAllBytes(path), charset)} -> {@code Files.readString(path, charset)}.
     * <p>
     * Behavioral note: {@code Files.readString} throws on malformed/unmappable input, whereas
     * {@code new String(byte[], Charset)} silently substitutes the Unicode replacement character.
     */
    @RecipeDescriptor(
            name = "Replace `new String(Files.readAllBytes(path), charset)` with `Files.readString(path, charset)`",
            description = "Use `Files.readString(Path, Charset)` instead of building a `String` from " +
                          "`Files.readAllBytes(Path)`."
    )
    public static class ReadString {
        @BeforeTemplate
        String before(Path path, Charset charset) throws IOException {
            return new String(Files.readAllBytes(path), charset);
        }

        @AfterTemplate
        String after(Path path, Charset charset) throws IOException {
            return Files.readString(path, charset);
        }
    }

    /**
     * {@code Files.write(path, s.getBytes(UTF_8), options)} -> {@code Files.writeString(path, s, options)}.
     * <p>
     * Only the UTF-8 form is rewritten without an explicit charset, because
     * {@code Files.writeString} encodes as UTF-8 by default.
     */
    @RecipeDescriptor(
            name = "Replace `Files.write(path, s.getBytes(UTF_8), options)` with `Files.writeString(path, s, options)`",
            description = "Use `Files.writeString(Path, CharSequence, OpenOption...)` instead of writing the " +
                          "UTF-8 encoded bytes of a `String`; `Files.writeString` uses UTF-8 by default."
    )
    public static class WriteString {
        @BeforeTemplate
        Path before(Path path, String content, @Repeated OpenOption options) throws IOException {
            return Files.write(path, content.getBytes(StandardCharsets.UTF_8), options);
        }

        @AfterTemplate
        Path after(Path path, String content, @Repeated OpenOption options) throws IOException {
            return Files.writeString(path, content, options);
        }
    }

    /**
     * {@code Files.write(path, s.getBytes(UTF_8))} -> {@code Files.writeString(path, s)}.
     * <p>
     * Separate template for the no-{@code OpenOption} form: the generated precondition for
     * {@link WriteString} requires a reference to {@code OpenOption}, which is absent when no
     * options are passed.
     */
    @RecipeDescriptor(
            name = "Replace `Files.write(path, s.getBytes(UTF_8))` with `Files.writeString(path, s)`",
            description = "Use `Files.writeString(Path, CharSequence)` instead of writing the UTF-8 encoded " +
                          "bytes of a `String`; `Files.writeString` uses UTF-8 by default."
    )
    public static class WriteStringNoOptions {
        @BeforeTemplate
        Path before(Path path, String content) throws IOException {
            return Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        }

        @AfterTemplate
        Path after(Path path, String content) throws IOException {
            return Files.writeString(path, content);
        }
    }

    /**
     * {@code Files.write(path, s.getBytes(charset), options)} -> {@code Files.writeString(path, s, charset, options)}.
     * <p>
     * General (non-UTF-8) charset form, which keeps an explicit {@link java.nio.charset.Charset} argument.
     * Declared after the UTF-8 templates so a UTF-8 call is matched by {@link WriteString} first and
     * loses its redundant charset, rather than being rewritten to {@code writeString(path, s, UTF_8, options)}.
     */
    @RecipeDescriptor(
            name = "Replace `Files.write(path, s.getBytes(charset), options)` with `Files.writeString(path, s, charset, options)`",
            description = "Use `Files.writeString(Path, CharSequence, Charset, OpenOption...)` instead of writing " +
                          "the `charset`-encoded bytes of a `String`."
    )
    public static class WriteStringCharset {
        @BeforeTemplate
        Path before(Path path, String content, Charset charset, @Repeated OpenOption options) throws IOException {
            return Files.write(path, content.getBytes(charset), options);
        }

        @AfterTemplate
        Path after(Path path, String content, Charset charset, @Repeated OpenOption options) throws IOException {
            return Files.writeString(path, content, charset, options);
        }
    }

    /**
     * {@code Files.write(path, s.getBytes(charset))} -> {@code Files.writeString(path, s, charset)}.
     * <p>
     * No-{@code OpenOption} counterpart of {@link WriteStringCharset}; see {@link WriteStringNoOptions}
     * for why a separate template is needed.
     */
    @RecipeDescriptor(
            name = "Replace `Files.write(path, s.getBytes(charset))` with `Files.writeString(path, s, charset)`",
            description = "Use `Files.writeString(Path, CharSequence, Charset)` instead of writing the " +
                          "`charset`-encoded bytes of a `String`."
    )
    public static class WriteStringCharsetNoOptions {
        @BeforeTemplate
        Path before(Path path, String content, Charset charset) throws IOException {
            return Files.write(path, content.getBytes(charset));
        }

        @AfterTemplate
        Path after(Path path, String content, Charset charset) throws IOException {
            return Files.writeString(path, content, charset);
        }
    }
}
