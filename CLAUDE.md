# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this project is

A library of **OpenRewrite recipes** that implement IntelliJ "Java language level migration aid"
inspections. It is *not* an application — it produces a jar of recipes that other projects apply via
the `rewrite-maven-plugin`. All recipes are authored as **Refaster templates**:
`@BeforeTemplate`/`@AfterTemplate` pairs grouped under `@RecipeDescriptor`. The `rewrite-templating`
annotation processor generates a `*Recipes` class at compile time into
`target/generated-sources/annotations`. Source files: `ReadWriteStringCanBeUsed`,
`StringIsEmptyCanBeUsed`, `StringIsBlankCanBeUsed`.

The whole project is MIT-licensed; there are no vendored/third-party recipe sources.

## Build & test

```bash
./mvnw clean test              # full build + tests
./mvnw test -Dtest=ReadWriteStringCanBeUsedTest          # single test class
./mvnw test -Dtest=StringEmptinessYamlTest#isBlankAppliesOnJava11   # single test method
```

Builds/runs on JDK 17, 21, 25. **The `--add-exports`/`--add-opens` flags are mandatory** because
OpenRewrite's parser and the Refaster processor reach into `com.sun.tools.javac` internals
(encapsulated on JDK 16+). They are pre-wired in two places: `.mvn/jvm.config` (compile time) and the
`maven-surefire-plugin` `argLine` (test time). Running `javac`/tests outside Maven will fail without them.

After editing a Refaster template, the generated `*Recipes` class only exists post-compile. If an IDE
shows `new ReadWriteStringCanBeUsedRecipes()` as unresolved, run `./mvnw test-compile` first.

## How the pieces connect

- A Refaster source class (e.g. `ReadWriteStringCanBeUsed`) → generates `ReadWriteStringCanBeUsedRecipes`.
- A declarative YAML under `src/main/resources/META-INF/rewrite/` wraps the generated recipe under a
  **stable public name** (e.g. `io.github.taodongl.UseFilesReadWriteString`) and is what end users
  activate. The generated class name is an internal detail; the YAML name is the contract.
- **Java-version gating lives in the YAML, not the template.** Recipes using Java 11+ APIs
  (`Files.readString`, `String.isBlank`) carry a `HasJavaVersion: "[11,)"` precondition.
  `String.isEmpty()` (Java 6) is ungated.

## Conventions that matter

- **Never silently change charset semantics.** Byte read/write idioms with no explicit charset
  (`new String(bytes)`, `s.getBytes()`) use the platform default; `Files.readString/writeString` use
  UTF-8. Those forms are deliberately left untouched. A UTF-8 form drops the redundant charset; any
  other charset is preserved. Tests `doesNotReplace*` lock this in — don't "fix" them.
- **Template declaration order is load-bearing.** UTF-8 write templates are declared *before* the
  general-charset ones so a UTF-8 call is matched first and loses its redundant charset rather than
  being rewritten to `writeString(path, s, UTF_8, ...)`. See `mixedCharsetsInOneFile` test.
- **Separate templates for the no-`OpenOption` forms** (`WriteStringNoOptions`,
  `WriteStringCharsetNoOptions`): the generated precondition for the `@Repeated OpenOption` variant
  requires an `OpenOption` reference, which is absent when no options are passed.
- Refaster matching relies on `-parameters` (compiler keeps parameter names) — already configured.

## Testing pattern

Tests implement `RewriteTest` and use `rewriteRun(java(before, after))`. A single-arg `java(before)`
asserts **no change**. Use `version(java(...), N)` to pin the source Java level — essential for the
gating tests (`StringEmptinessYamlTest`). YAML wiring is tested via `spec.recipeFromResource(path, name)`;
generated/imperative recipes via `spec.recipe(new ...Recipes())`.

When adding a recipe: write the Refaster template (or `Recipe` subclass), add/extend the YAML wrapper
with the right version precondition, add a `*Test`, and update `README.md`'s recipe table.

## Releasing

Publishing to Maven Central is automated via `.github/workflows/release.yml` (on GitHub Release publish
or manual dispatch). It runs `./mvnw -Prelease clean deploy`. The `release` profile attaches
sources+javadoc jars, GPG-signs everything, and uploads through the Sonatype Central Portal plugin
(`autoPublish=false` — review in the Central UI before releasing). Requires the
`CENTRAL_TOKEN_*`/`GPG_*` repo secrets.
