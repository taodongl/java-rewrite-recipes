package io.github.taodongl;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.version;

/**
 * Validates the declarative recipes and, crucially, the Java-version gating:
 * {@code UseStringIsEmpty} applies on any version, while {@code UseStringIsBlank} only applies on
 * Java 11+.
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
    void isBlankAppliesOnJava11() {
        rewriteRun(
          spec -> spec.recipeFromResource(
            "/META-INF/rewrite/string-emptiness.yml",
            "io.github.taodongl.UseStringIsBlank"
          ),
          version(
            java(
              """
                class A {
                    boolean blank(String s) {
                        return s.trim().length() == 0;
                    }
                }
                """,
              """
                class A {
                    boolean blank(String s) {
                        return s.isBlank();
                    }
                }
                """
            ),
            11
          )
        );
    }

    @Test
    void isBlankDoesNotApplyOnJava8() {
        // String.isBlank() does not exist before Java 11, so the HasJavaVersion precondition
        // blocks the rewrite and the trim()-based check is left unchanged.
        rewriteRun(
          spec -> spec.recipeFromResource(
            "/META-INF/rewrite/string-emptiness.yml",
            "io.github.taodongl.UseStringIsBlank"
          ),
          version(
            java(
              """
                class A {
                    boolean blank(String s) {
                        return s.trim().length() == 0;
                    }
                }
                """
            ),
            8
          )
        );
    }
}
