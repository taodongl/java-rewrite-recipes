# Third-Party Notices

This project is licensed under the MIT License (see `LICENSE`), **except** for the files listed
below, which are derived from [openrewrite/rewrite-migrate-java](https://github.com/openrewrite/rewrite-migrate-java)
and are licensed under the **Moderne Source Available License (MSAL)**, not MIT.

| File | Origin | License |
| --- | --- | --- |
| `src/main/java/io/github/taodongl/SwitchCaseReturnsToSwitchExpression.java` | `org.openrewrite.java.migrate.lang.SwitchCaseReturnsToSwitchExpression` | Moderne Source Available License |
| `src/main/java/io/github/taodongl/SwitchUtils.java` | `org.openrewrite.java.migrate.lang.SwitchUtils` | Moderne Source Available License |

Each of these files retains its original MSAL copyright/license header. By distributing this
artifact you are redistributing MSAL-licensed material and must comply with the terms of the
Moderne Source Available License: https://docs.moderne.io/licensing/moderne-source-available-license

Modifications made to the copied files are noted in their header comments (package change, Java
version gate lowered from 21 to 14, Lombok removed, and added support for `throw` cases).
