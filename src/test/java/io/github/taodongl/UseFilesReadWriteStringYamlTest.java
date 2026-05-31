package io.github.taodongl;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.version;

/**
 * Validates the declarative recipe in {@code META-INF/rewrite/read-write-string.yml}, including
 * its {@code HasJavaVersion} precondition that restricts the rewrite to Java 11+ source files.
 */
class UseFilesReadWriteStringYamlTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResource(
          "/META-INF/rewrite/read-write-string.yml",
          "io.github.taodongl.UseFilesReadWriteString"
        );
    }

    @Test
    void appliesOnJava11() {
        rewriteRun(
          version(
            java(
              """
                import java.io.IOException;
                import java.nio.charset.StandardCharsets;
                import java.nio.file.Files;
                import java.nio.file.Path;
                import java.nio.file.StandardOpenOption;

                class A {
                    String roundTrip(Path in, Path out, String s) throws IOException {
                        Files.write(out, s.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
                        return new String(Files.readAllBytes(in), StandardCharsets.ISO_8859_1);
                    }
                }
                """,
              """
                import java.io.IOException;
                import java.nio.charset.StandardCharsets;
                import java.nio.file.Files;
                import java.nio.file.Path;
                import java.nio.file.StandardOpenOption;

                class A {
                    String roundTrip(Path in, Path out, String s) throws IOException {
                        Files.writeString(out, s, StandardOpenOption.WRITE);
                        return Files.readString(in, StandardCharsets.ISO_8859_1);
                    }
                }
                """
            ),
            11
          )
        );
    }

    @Test
    void doesNotApplyOnJava8() {
        // Same input, but compiled at Java 8: Files.readString/writeString don't exist there,
        // so the HasJavaVersion precondition blocks the recipe and nothing changes.
        rewriteRun(
          version(
            java(
              """
                import java.io.IOException;
                import java.nio.charset.StandardCharsets;
                import java.nio.file.Files;
                import java.nio.file.Path;
                import java.nio.file.StandardOpenOption;

                class A {
                    String roundTrip(Path in, Path out, String s) throws IOException {
                        Files.write(out, s.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
                        return new String(Files.readAllBytes(in), StandardCharsets.ISO_8859_1);
                    }
                }
                """
            ),
            8
          )
        );
    }
}
