package io.github.taodongl;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.version;

/**
 * Validates the {@code UseStringIsEmpty} declarative recipe wiring. It applies on any Java version
 * ({@code String.isEmpty()} exists since Java 6), so it carries no version gate.
 */
class StringEmptinessYamlTest implements RewriteTest {

    @Test
    void isEmptyAppliesOnJava8() {
        rewriteRun(
          spec -> spec.recipeFromResource(
            "/META-INF/rewrite/string-emptiness.yml",
            "io.github.taodongl.UseStringIsEmpty"
          ),
          version(
            java(
              """
                class A {
                    boolean empty(String s) {
                        return s.length() == 0;
                    }
                }
                """,
              """
                class A {
                    boolean empty(String s) {
                        return s.isEmpty();
                    }
                }
                """
            ),
            8
          )
        );
    }

    @Test
    void equalsEmptyStringApplies() {
        rewriteRun(
          spec -> spec.recipeFromResource(
            "/META-INF/rewrite/string-emptiness.yml",
            "io.github.taodongl.UseStringIsEmpty"
          ),
          version(
            java(
              """
                class A {
                    boolean empty(String s) {
                        return s.equals("");
                    }
                }
                """,
              """
                class A {
                    boolean empty(String s) {
                        return s.isEmpty();
                    }
                }
                """
            ),
            8
          )
        );
    }
}
