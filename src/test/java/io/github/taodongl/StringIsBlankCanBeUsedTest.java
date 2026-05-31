package io.github.taodongl;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * Unit tests for the transformation logic of the generated {@code StringIsBlankCanBeUsedRecipes}.
 * Java-version gating is verified separately in {@link StringEmptinessYamlTest}.
 */
class StringIsBlankCanBeUsedTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new StringIsBlankCanBeUsedRecipes());
    }

    @Test
    void trimLengthEqualsZero() {
        rewriteRun(
          java(
            """
              class A {
                  boolean blank(String subProc) {
                      return subProc.trim().length() == 0;
                  }
              }
              """,
            """
              class A {
                  boolean blank(String subProc) {
                      return subProc.isBlank();
                  }
              }
              """
          )
        );
    }

    @Test
    void trimEqualsEmptyString() {
        rewriteRun(
          java(
            """
              class A {
                  boolean blank(String location) {
                      return location.trim().equals("");
                  }
              }
              """,
            """
              class A {
                  boolean blank(String location) {
                      return location.isBlank();
                  }
              }
              """
          )
        );
    }

    @Test
    void trimIsEmpty() {
        rewriteRun(
          java(
            """
              class A {
                  boolean blank(String s) {
                      return s.trim().isEmpty();
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
          )
        );
    }

    @Test
    void blankMirrorVariants() {
        rewriteRun(
          java(
            """
              class A {
                  boolean a(String s) { return 0 == s.trim().length(); }
                  boolean b(String location) { return "".equals(location.trim()); }
              }
              """,
            """
              class A {
                  boolean a(String s) { return s.isBlank(); }
                  boolean b(String location) { return location.isBlank(); }
              }
              """
          )
        );
    }

    @Test
    void notBlankVariants() {
        rewriteRun(
          java(
            """
              class A {
                  boolean a(String subProc) { return subProc.trim().length() > 0; }
                  boolean b(String s) { return s.trim().length() != 0; }
                  boolean c(String s) { return 0 != s.trim().length(); }
                  boolean d(String s) { return 0 < s.trim().length(); }
                  boolean e(String s) { return !s.trim().isEmpty(); }
              }
              """,
            """
              class A {
                  boolean a(String subProc) { return !subProc.isBlank(); }
                  boolean b(String s) { return !s.isBlank(); }
                  boolean c(String s) { return !s.isBlank(); }
                  boolean d(String s) { return !s.isBlank(); }
                  boolean e(String s) { return !s.isBlank(); }
              }
              """
          )
        );
    }
}
