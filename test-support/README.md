# Integration test convention

Every Stonecutter target uses the tests in this directory.

When adding or updating a compatibility integration:

1. Add its mod id to `ModIntegration`.
2. Add the exact dependency version to the target build script.
3. Add or update its row in that target's `integration-contracts.txt`.
4. Use `REQUIRED` plus a class/member contract whenever the dependency is resolvable.
5. Use `DEFERRED` only when the artifact cannot be resolved, and include the reason.
6. Run `./gradlew integrationTest` before committing.

Contract format:

```text
MODE|mod_id|fully.qualified.Class|memberName|JVM descriptor|note
```

An empty member checks only class presence. Constructors use `<init>`. The tests inspect
bytecode and do not initialize optional mod classes.
