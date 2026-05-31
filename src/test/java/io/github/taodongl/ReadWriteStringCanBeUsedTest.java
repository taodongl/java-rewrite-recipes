package io.github.taodongl;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class ReadWriteStringCanBeUsedTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ReadWriteStringCanBeUsedRecipes());
    }

    @Test
    void readStringWithCharset() {
        rewriteRun(
          java(
            """
              import java.io.IOException;
              import java.nio.charset.StandardCharsets;
              import java.nio.file.Files;
              import java.nio.file.Path;

              class A {
                  String read(Path path) throws IOException {
                      return new String(Files.readAllBytes(path), StandardCharsets.ISO_8859_1);
                  }
              }
              """,
            """
              import java.io.IOException;
              import java.nio.charset.StandardCharsets;
              import java.nio.file.Files;
              import java.nio.file.Path;

              class A {
                  String read(Path path) throws IOException {
                      return Files.readString(path, StandardCharsets.ISO_8859_1);
                  }
              }
              """
          )
        );
    }

    @Test
    void writeStringUtf8() {
        rewriteRun(
          java(
            """
              import java.io.IOException;
              import java.nio.charset.StandardCharsets;
              import java.nio.file.Files;
              import java.nio.file.Path;
              import java.nio.file.StandardOpenOption;

              class A {
                  void write(Path path, String s) throws IOException {
                      Files.write(path, s.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
                  }
              }
              """,
            """
              import java.io.IOException;
              import java.nio.file.Files;
              import java.nio.file.Path;
              import java.nio.file.StandardOpenOption;

              class A {
                  void write(Path path, String s) throws IOException {
                      Files.writeString(path, s, StandardOpenOption.WRITE);
                  }
              }
              """
          )
        );
    }

    @Test
    void writeStringUtf8NoOptions() {
        rewriteRun(
          java(
            """
              import java.io.IOException;
              import java.nio.charset.StandardCharsets;
              import java.nio.file.Files;
              import java.nio.file.Path;

              class A {
                  void write(Path path, String s) throws IOException {
                      Files.write(path, s.getBytes(StandardCharsets.UTF_8));
                  }
              }
              """,
            """
              import java.io.IOException;
              import java.nio.file.Files;
              import java.nio.file.Path;

              class A {
                  void write(Path path, String s) throws IOException {
                      Files.writeString(path, s);
                  }
              }
              """
          )
        );
    }

    @Test
    void writeStringWithNonUtf8CharsetKeepsCharset() {
        rewriteRun(
          java(
            """
              import java.io.IOException;
              import java.nio.charset.StandardCharsets;
              import java.nio.file.Files;
              import java.nio.file.Path;

              class A {
                  void write(Path path, String s) throws IOException {
                      Files.write(path, s.getBytes(StandardCharsets.ISO_8859_1));
                  }
              }
              """,
            """
              import java.io.IOException;
              import java.nio.charset.StandardCharsets;
              import java.nio.file.Files;
              import java.nio.file.Path;

              class A {
                  void write(Path path, String s) throws IOException {
                      Files.writeString(path, s, StandardCharsets.ISO_8859_1);
                  }
              }
              """
          )
        );
    }

    /**
     * Proves the path argument is matched structurally by type, so {@code Path.of(...)} works just
     * as well as {@code Paths.get(...)} — the template never hard-codes either factory.
     */
    @Test
    void worksWithPathOfFactory() {
        rewriteRun(
          java(
            """
              import java.io.IOException;
              import java.nio.charset.StandardCharsets;
              import java.nio.file.Files;
              import java.nio.file.Path;

              class A {
                  String read() throws IOException {
                      return new String(Files.readAllBytes(Path.of("in.txt")), StandardCharsets.UTF_8);
                  }
              }
              """,
            """
              import java.io.IOException;
              import java.nio.charset.StandardCharsets;
              import java.nio.file.Files;
              import java.nio.file.Path;

              class A {
                  String read() throws IOException {
                      return Files.readString(Path.of("in.txt"), StandardCharsets.UTF_8);
                  }
              }
              """
          )
        );
    }

    /**
     * Proves recipe ordering: in one file, a UTF-8 write drops its charset while a non-UTF-8 write
     * keeps it. The UTF-8 templates are declared before the general-charset ones.
     */
    @Test
    void mixedCharsetsInOneFile() {
        rewriteRun(
          java(
            """
              import java.io.IOException;
              import java.nio.charset.StandardCharsets;
              import java.nio.file.Files;
              import java.nio.file.Path;

              class A {
                  void write(Path utf8, Path iso, String s) throws IOException {
                      Files.write(utf8, s.getBytes(StandardCharsets.UTF_8));
                      Files.write(iso, s.getBytes(StandardCharsets.ISO_8859_1));
                  }
              }
              """,
            """
              import java.io.IOException;
              import java.nio.charset.StandardCharsets;
              import java.nio.file.Files;
              import java.nio.file.Path;

              class A {
                  void write(Path utf8, Path iso, String s) throws IOException {
                      Files.writeString(utf8, s);
                      Files.writeString(iso, s, StandardCharsets.ISO_8859_1);
                  }
              }
              """
          )
        );
    }

    /**
     * Must NOT change: {@code new String(byte[])} uses the platform default charset, whereas
     * {@code Files.readString(Path)} always uses UTF-8. Rewriting would silently change the encoding.
     */
    @Test
    void doesNotReplaceReadWithoutExplicitCharset() {
        rewriteRun(
          java(
            """
              import java.io.IOException;
              import java.nio.file.Files;
              import java.nio.file.Path;

              class A {
                  String read(Path path) throws IOException {
                      return new String(Files.readAllBytes(path));
                  }
              }
              """
          )
        );
    }

    /**
     * Must NOT change: {@code String.getBytes()} uses the platform default charset, whereas
     * {@code Files.writeString(Path, CharSequence)} always uses UTF-8.
     */
    @Test
    void doesNotReplaceWriteWithoutExplicitCharset() {
        rewriteRun(
          java(
            """
              import java.io.IOException;
              import java.nio.file.Files;
              import java.nio.file.Path;

              class A {
                  void write(Path path, String s) throws IOException {
                      Files.write(path, s.getBytes());
                  }
              }
              """
          )
        );
    }

    /**
     * Must NOT change: the {@code getBytes(String charsetName)} overload (a charset *name*, not a
     * {@code Charset}) is a different signature and is intentionally out of scope.
     */
    @Test
    void doesNotReplaceWriteWithCharsetName() {
        rewriteRun(
          java(
            """
              import java.io.IOException;
              import java.io.UnsupportedEncodingException;
              import java.nio.file.Files;
              import java.nio.file.Path;

              class A {
                  void write(Path path, String s) throws IOException {
                      Files.write(path, s.getBytes("UTF-8"));
                  }
              }
              """
          )
        );
    }
}
