package io.github.taodongl;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.openrewrite.java.template.RecipeDescriptor;

/**
 * Refaster templates replacing {@code String.length()} comparisons against zero with
 * {@link String#isEmpty()}.
 * <p>
 * {@code String.isEmpty()} has existed since Java 6, so these recipes need no Java version gate.
 * <p>
 * Each recipe groups several equivalent source forms (including operand-swapped "mirror" variants)
 * under a single {@code @AfterTemplate} by declaring multiple {@code @BeforeTemplate} methods.
 */
@RecipeDescriptor(
        name = "`String.isEmpty()` can be used",
        description = "Replace `String.length()` comparisons against zero with `String.isEmpty()`."
)
public class StringIsEmptyCanBeUsed {

    /**
     * {@code s.length() == 0} / {@code 0 == s.length()} -> {@code s.isEmpty()}.
     */
    @RecipeDescriptor(
            name = "Replace `s.length() == 0` with `s.isEmpty()`",
            description = "Use `String.isEmpty()` instead of comparing `String.length()` to zero."
    )
    public static class IsEmpty {
        @BeforeTemplate
        boolean lengthEqualsZero(String s) {
            return s.length() == 0;
        }

        @BeforeTemplate
        boolean zeroEqualsLength(String s) {
            return 0 == s.length();
        }

        @AfterTemplate
        boolean after(String s) {
            return s.isEmpty();
        }
    }

    /**
     * {@code s.length() != 0} / {@code 0 != s.length()} / {@code s.length() > 0} /
     * {@code 0 < s.length()} -> {@code !s.isEmpty()}.
     */
    @RecipeDescriptor(
            name = "Replace `s.length() != 0` / `s.length() > 0` with `!s.isEmpty()`",
            description = "Use `!String.isEmpty()` instead of comparing `String.length()` against zero."
    )
    public static class IsNotEmpty {
        @BeforeTemplate
        boolean lengthNotEqualsZero(String s) {
            return s.length() != 0;
        }

        @BeforeTemplate
        boolean zeroNotEqualsLength(String s) {
            return 0 != s.length();
        }

        @BeforeTemplate
        boolean lengthGreaterThanZero(String s) {
            return s.length() > 0;
        }

        @BeforeTemplate
        boolean zeroLessThanLength(String s) {
            return 0 < s.length();
        }

        @AfterTemplate
        boolean after(String s) {
            return !s.isEmpty();
        }
    }
}
