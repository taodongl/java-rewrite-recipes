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
}
