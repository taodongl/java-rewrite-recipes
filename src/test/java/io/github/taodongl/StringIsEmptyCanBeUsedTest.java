package io.github.taodongl;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class StringIsEmptyCanBeUsedTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new StringIsEmptyCanBeUsedRecipes());
    }

    @Test
    void lengthEqualsZero() {
        rewriteRun(
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
          )
        );
    }

    @Test
    void lengthGreaterThanZero() {
        rewriteRun(
          java(
            """
              class A {
                  boolean notEmpty(String s) {
                      return s.length() > 0;
                  }
              }
              """,
            """
              class A {
                  boolean notEmpty(String s) {
                      return !s.isEmpty();
                  }
              }
              """
          )
        );
    }

    @Test
    void mirrorAndInequalityVariants() {
        rewriteRun(
          java(
            """
              class A {
                  boolean a(String s) { return 0 == s.length(); }
                  boolean b(String s) { return s.length() != 0; }
                  boolean c(String s) { return 0 != s.length(); }
                  boolean d(String s) { return 0 < s.length(); }
              }
              """,
            """
              class A {
                  boolean a(String s) { return s.isEmpty(); }
                  boolean b(String s) { return !s.isEmpty(); }
                  boolean c(String s) { return !s.isEmpty(); }
                  boolean d(String s) { return !s.isEmpty(); }
              }
              """
          )
        );
    }

    @Test
    void equalsEmptyString() {
        rewriteRun(
          java(
            """
              class A {
                  boolean a(String s) { return s.equals(""); }
              }
              """,
            """
              class A {
                  boolean a(String s) { return s.isEmpty(); }
              }
              """
          )
        );
    }

    /**
     * {@code "".equals(s)} is null-safe (returns {@code false} for {@code null}) while
     * {@code s.isEmpty()} throws, so the mirror form is deliberately left untouched.
     */
    @Test
    void doesNotReplaceNullSafeMirror() {
        rewriteRun(
          java(
            """
              class A {
                  boolean b(String s) { return "".equals(s); }
              }
              """
          )
        );
    }

    /**
     * The {@code equals("")} templates are generic over any {@code String} expression, so a trimmed
     * receiver becomes {@code s.trim().isEmpty()} — matching IntelliJ, which no longer collapses
     * {@code s.trim().equals("")} to {@code s.isBlank()}.
     */
    @Test
    void trimmedReceiverBecomesTrimIsEmpty() {
        rewriteRun(
          java(
            """
              class A {
                  boolean a(String s) { return s.trim().equals(""); }
                  boolean b(String s) { return s.trim().length() == 0; }
              }
              """,
            """
              class A {
                  boolean a(String s) { return s.trim().isEmpty(); }
                  boolean b(String s) { return s.trim().isEmpty(); }
              }
              """
          )
        );
    }
}
