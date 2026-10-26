# Third-party notices

The root MIT LICENSE applies to this project's original work. It does not replace third-party notices or grant rights to Minecraft or independently installed dependencies.

## Gradle Wrapper (source repository only)

`gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar` are Gradle tooling distributed under Apache License 2.0. The script copyright and license headers are preserved; the JAR contains `META-INF/LICENSE`. A copy of that embedded license is provided at [licenses/Gradle-Wrapper-Apache-2.0.txt](licenses/Gradle-Wrapper-Apache-2.0.txt). These files are not included in the mod JAR.

The wrapper script headers state Copyright 2015 the original author or authors. Gradle upstream: <https://github.com/gradle/gradle>. Distribution selected by the existing wrapper: 9.6.1. This does not independently establish the generation version of the checked-in wrapper JAR.

## Build and runtime dependencies

Minecraft is obtained by Loom for building and is not redistributed by this repository or the mod JAR. Fabric Loader and Fabric API are separately installed dependencies; JUnit 4.13.2 is a test dependency. No dependency JAR is intentionally shaded or nested by this project's build. Their own licensing applies; root MIT does not relicense them. Gradle caches and the downloaded distribution remain ignored.

## Assets and audit limits (2026-10-26)

The source assets consist of language JSON files and an item model definition referencing `minecraft:item/firework_rocket`; no vanilla texture or model file is copied into the repository. GUI decoration is implemented in Java. The tracked `docs/phase05b1-quality-comparison.png` is a development comparison image, outside the mod resources and not included in the mod JAR.

Static inspection found no additional third-party copyright headers in `src/` or `tests/`. This is an inventory of identifiable files and notices, not a proof of the authorship of every historical contribution or the provenance of the documentation image. Existing headers have not been removed or replaced.
