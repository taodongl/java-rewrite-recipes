package io.github.taodongl;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.version;

class SwitchCaseReturnsToSwitchExpressionTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new SwitchCaseReturnsToSwitchExpression());
    }

    @Test
    void allCasesReturnWithReturningDefault() {
        rewriteRun(
          version(
            java(
              """
                class A {
                    String f(String s) {
                        switch (s) {
                            case "a":
                                return "x";
                            default:
                                return "y";
                        }
                    }
                }
                """,
              """
                class A {
                    String f(String s) {
                        return switch (s) {
                            case "a" -> "x";
                            default -> "y";
                        };
                    }
                }
                """
            ),
            17
          )
        );
    }

    @Test
    void usersExampleWithThrowingDefault() {
        rewriteRun(
          version(
            java(
              """
                class A {
                    double getPrice(String fruit) {
                        switch (fruit) {
                            case "Apple":
                                return 1.0;
                            case "Orange":
                                return 1.5;
                            case "Mango":
                                return 2.0;
                            default:
                                throw new IllegalArgumentException();
                        }
                    }
                }
                """,
              """
                class A {
                    double getPrice(String fruit) {
                        return switch (fruit) {
                            case "Apple" -> 1.0;
                            case "Orange" -> 1.5;
                            case "Mango" -> 2.0;
                            default -> throw new IllegalArgumentException();
                        };
                    }
                }
                """
            ),
            17
          )
        );
    }

    @Test
    void doesNotApplyBelowJava14() {
        rewriteRun(
          version(
            java(
              """
                class A {
                    String f(String s) {
                        switch (s) {
                            case "a":
                                return "x";
                            default:
                                return "y";
                        }
                    }
                }
                """
            ),
            13
          )
        );
    }
}
