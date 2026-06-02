package io.github.taodongl;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.openrewrite.java.template.RecipeDescriptor;

/**
 * Refaster templates replacing {@code String.length()} comparisons against zero and
 * {@code equals("")} checks with {@link String#isEmpty()}.
 * <p>
 * {@code String.isEmpty()} has existed since Java 6, so these recipes need no Java version gate.
 * <p>
 * Each recipe groups several equivalent source forms (including operand-swapped "mirror" variants)
 * under a single {@code @AfterTemplate} by declaring multiple {@code @BeforeTemplate} methods.
 */
@RecipeDescriptor(
        name = "`String.isEmpty()` can be used",
        description = "Replace `String.length()` comparisons against zero and `equals(\"\")` checks with " +
                      "`String.isEmpty()`."
)
public class StringIsEmptyCanBeUsed {

    /**
     * {@code s.length() == 0} / {@code 0 == s.length()} / {@code s.equals("")} -> {@code s.isEmpty()}.
     * <p>
     * Null-safety note: only the {@code s.equals("")} form (receiver {@code s}) is rewritten. Like
     * {@code s.isEmpty()}, it throws {@code NullPointerException} when {@code s} is {@code null}, so the
     * transform preserves behavior. The mirror form {@code "".equals(s)} is deliberately <em>not</em>
     * matched: it returns {@code false} for a {@code null} {@code s}, whereas {@code s.isEmpty()} would
     * throw — rewriting it would change semantics.
     */
    @RecipeDescriptor(
            name = "Replace `s.length() == 0` / `s.equals(\"\")` with `s.isEmpty()`",
            description = "Use `String.isEmpty()` instead of comparing `String.length()` to zero or " +
                          "checking equality with the empty string."
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

        @BeforeTemplate
        boolean equalsEmptyString(String s) {
            return s.equals("");
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
