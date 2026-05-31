package io.github.taodongl;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.openrewrite.java.template.RecipeDescriptor;

/**
 * Refaster templates replacing {@code String.trim()}-based emptiness checks with
 * {@link String#isBlank()}.
 * <p>
 * <strong>Java version:</strong> {@code String.isBlank()} was introduced in <strong>Java 11</strong>.
 * These generated recipes carry no version check themselves; the gate is applied at the declarative
 * layer (see {@code io.github.taodongl.UseStringIsBlank} in {@code META-INF/rewrite/string-emptiness.yml},
 * which adds a {@code HasJavaVersion: [11,)} precondition).
 * <p>
 * <strong>Behavioral note:</strong> {@code trim()} strips only characters {@code <= U+0020}, whereas
 * {@code isBlank()} uses {@code Character.isWhitespace(int)}. The two definitions of "whitespace"
 * differ for some Unicode characters, so the result can change for strings containing exotic
 * whitespace/control characters. This matches IntelliJ's inspection behavior.
 * <p>
 * Each recipe groups several equivalent source forms (operand-swapped mirrors and {@code length()} /
 * {@code equals("")} / {@code isEmpty()} spellings) under a single {@code @AfterTemplate}.
 */
@RecipeDescriptor(
        name = "`String.isBlank()` can be used",
        description = "Replace `String.trim()`-based emptiness checks with `String.isBlank()` (Java 11+)."
)
public class StringIsBlankCanBeUsed {

    /**
     * {@code s.trim().length() == 0} / {@code 0 == s.trim().length()} / {@code s.trim().equals("")} /
     * {@code "".equals(s.trim())} / {@code s.trim().isEmpty()} -> {@code s.isBlank()}.
     */
    @RecipeDescriptor(
            name = "Replace `s.trim()` emptiness checks with `s.isBlank()`",
            description = "Use `String.isBlank()` instead of checking that `String.trim()` is empty."
    )
    public static class IsBlank {
        @BeforeTemplate
        boolean trimLengthEqualsZero(String s) {
            return s.trim().length() == 0;
        }

        @BeforeTemplate
        boolean zeroEqualsTrimLength(String s) {
            return 0 == s.trim().length();
        }

        @BeforeTemplate
        boolean trimEqualsEmpty(String s) {
            return s.trim().equals("");
        }

        @BeforeTemplate
        boolean emptyEqualsTrim(String s) {
            return "".equals(s.trim());
        }

        @BeforeTemplate
        boolean trimIsEmpty(String s) {
            return s.trim().isEmpty();
        }

        @AfterTemplate
        boolean after(String s) {
            return s.isBlank();
        }
    }

    /**
     * {@code s.trim().length() != 0} / {@code 0 != s.trim().length()} / {@code s.trim().length() > 0} /
     * {@code 0 < s.trim().length()} / {@code !s.trim().isEmpty()} -> {@code !s.isBlank()}.
     */
    @RecipeDescriptor(
            name = "Replace negated `s.trim()` emptiness checks with `!s.isBlank()`",
            description = "Use `!String.isBlank()` instead of checking that `String.trim()` is non-empty."
    )
    public static class IsNotBlank {
        @BeforeTemplate
        boolean trimLengthNotEqualsZero(String s) {
            return s.trim().length() != 0;
        }

        @BeforeTemplate
        boolean zeroNotEqualsTrimLength(String s) {
            return 0 != s.trim().length();
        }

        @BeforeTemplate
        boolean trimLengthGreaterThanZero(String s) {
            return s.trim().length() > 0;
        }

        @BeforeTemplate
        boolean zeroLessThanTrimLength(String s) {
            return 0 < s.trim().length();
        }

        @BeforeTemplate
        boolean trimIsNotEmpty(String s) {
            return !s.trim().isEmpty();
        }

        @AfterTemplate
        boolean after(String s) {
            return !s.isBlank();
        }
    }
}
