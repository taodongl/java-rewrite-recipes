# java-rewrite-recipes

OpenRewrite recipes authored with **Refaster templates**, implementing several IntelliJ
"Java language level migration aids" inspections.

## Public recipes

| Declarative name | What it does | Java gate |
| --- | --- | --- |
| `io.github.taodongl.UseFilesReadWriteString` | `Files` byte read/write → `Files.readString()` / `writeString()` | **11+** |
| `io.github.taodongl.UseStringIsEmpty` | `String.length()` vs zero → `String.isEmpty()` | none (Java 6+) |
| `io.github.taodongl.UseStringIsBlank` | `String.trim()` emptiness checks → `String.isBlank()` | **11+** |

## Transformations

### `UseFilesReadWriteString` ([`ReadWriteStringCanBeUsed`](https://www.jetbrains.com/help/inspectopedia/ReadWriteStringCanBeUsed.html))

| Before | After |
| --- | --- |
| `new String(Files.readAllBytes(path), charset)` | `Files.readString(path, charset)` |
| `Files.write(path, s.getBytes(UTF_8), options)` | `Files.writeString(path, s, options)` |
| `Files.write(path, s.getBytes(charset), options)` | `Files.writeString(path, s, charset, options)` |

### `UseStringIsEmpty` / `UseStringIsBlank`

| Before | After |
| --- | --- |
| `s.length() == 0` | `s.isEmpty()` |
| `s.length() > 0` | `!s.isEmpty()` |
| `s.trim().length() == 0` | `s.isBlank()` |
| `s.trim().equals("")` | `s.isBlank()` |
| `s.trim().isEmpty()` | `s.isBlank()` |

Notes:
- **Default charset is never silently changed.** Byte read/write idioms with *no* explicit charset
  (`new String(bytes)`, `s.getBytes()`) use the platform default charset, while
  `Files.readString`/`writeString` always use UTF-8 — so those forms are deliberately left untouched.
  A UTF-8 write drops the redundant charset; any other charset is preserved.
- **`isBlank()` is Java 11+**, so `UseStringIsBlank` carries a `HasJavaVersion: [11,)` precondition;
  `isEmpty()` (Java 6) is ungated. The gate lives in the declarative YAML, not in the generated recipe.
- Behavioral nuances (match IntelliJ): `Files.readString()` throws on malformed input whereas
  `new String(byte[], Charset)` substitutes the replacement character; `trim()` strips only
  `<= U+0020` whereas `isBlank()` uses `Character.isWhitespace()`.

## Project layout

- `src/main/java/org/example/ReadWriteStringCanBeUsed.java` — the Refaster template
  (`@BeforeTemplate` / `@AfterTemplate`, grouped with `@RecipeDescriptor`). The
  `rewrite-templating` annotation processor generates `ReadWriteStringCanBeUsedRecipes` at compile
  time into `target/generated-sources/annotations`.
- `src/main/resources/META-INF/rewrite/read-write-string.yml` — declarative recipe
  `io.github.taodongl.UseFilesReadWriteString` that exposes the generated recipes under a stable name.
- `src/test/java/...` — `RewriteTest`-based tests for the generated recipes and the YAML wiring.

## Build & test

```bash
./mvnw clean test
```

Builds on JDK 17, 21, and 25. OpenRewrite's Java parser and the Refaster annotation processor reach
into `com.sun.tools.javac` internals, which are encapsulated on JDK 16+; the required
`--add-exports` / `--add-opens` flags are supplied via `.mvn/jvm.config` (compile time) and the
`maven-surefire-plugin` `argLine` (test time).

## Applying the recipe to another project

```bash
mvn org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.activeRecipes=io.github.taodongl.UseFilesReadWriteString \
  -Drewrite.recipeArtifactCoordinates=io.github.taodongl:java-rewrite-recipes:1.0
```
