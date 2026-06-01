# java-rewrite-recipes

OpenRewrite recipes authored with **Refaster templates**, implementing several IntelliJ
"Java language level migration aids" inspections.

## Public recipes

| Declarative name | What it does | Java gate |
| --- | --- | --- |
| `io.github.taodongl.UseFilesReadWriteString` | `Files` byte read/write → `Files.readString()` / `writeString()` | **11+** |
| `io.github.taodongl.UseStringIsEmpty` | `String.length()` vs zero / `equals("")` → `String.isEmpty()` | none (Java 6+) |

## Transformations

### `UseFilesReadWriteString` ([`ReadWriteStringCanBeUsed`](https://www.jetbrains.com/help/inspectopedia/ReadWriteStringCanBeUsed.html))

| Before | After |
| --- | --- |
| `new String(Files.readAllBytes(path), charset)` | `Files.readString(path, charset)` |
| `Files.write(path, s.getBytes(UTF_8), options)` | `Files.writeString(path, s, options)` |
| `Files.write(path, s.getBytes(charset), options)` | `Files.writeString(path, s, charset, options)` |

### `UseStringIsEmpty`

| Before | After |
| --- | --- |
| `s.length() == 0` | `s.isEmpty()` |
| `s.length() > 0` | `!s.isEmpty()` |
| `s.equals("")` | `s.isEmpty()` |
| `"".equals(s)` | `s.isEmpty()` |

The templates are generic over any `String` expression, so a trimmed receiver is handled too:
`s.trim().equals("")` and `s.trim().length() == 0` become `s.trim().isEmpty()` (matching IntelliJ,
which no longer collapses these to `s.isBlank()`).

Notes:
- **Default charset is never silently changed.** Byte read/write idioms with *no* explicit charset
  (`new String(bytes)`, `s.getBytes()`) use the platform default charset, while
  `Files.readString`/`writeString` always use UTF-8 — so those forms are deliberately left untouched.
  A UTF-8 write drops the redundant charset; any other charset is preserved.
- Behavioral nuances (match IntelliJ): `Files.readString()` throws on malformed input whereas
  `new String(byte[], Charset)` substitutes the replacement character; `"".equals(s)` is null-safe
  whereas the resulting `s.isEmpty()` throws on a `null` receiver.

## Project layout

- `src/main/java/io/github/taodongl/ReadWriteStringCanBeUsed.java` — the Refaster template
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
