# Repository Guidelines

## Project Structure & Module Organization
Rankademy uses a layered domain-first layout. Core APIs live in `src/main/java/maruhxn/rankademy`, grouped by domain packages such as `domain/user` and `domain/team`. Generated QueryDSL models compile into `src/main/generated`; run `./gradlew cleanQuerydsl` when removing Q classes. Configuration and secrets templates sit in `src/main/resources` (e.g., `application.yml`, `oauth2.yml`). Tests and fixtures live under `src/test/java` and `src/test/resources`.

## Build, Test, and Development Commands
- `./gradlew bootRun` – start the Spring Boot API with the default profiles.
- `./gradlew build` – compile, run unit tests, and package the application.
- `./gradlew test` – run the JUnit 5 suite with the Mockito javaagent.
- `./gradlew spotbugsMain` – static analysis; fix warnings before merging.
- `docker compose up -d` – bring up local dependencies defined in `docker-compose.yml`.

## Coding Style & Naming Conventions
Target Java 21 with 4-space indentation. Use `@Service`, `@Repository`, and domain-driven package names (`domain/user`, `domain/team`). Public classes should be PascalCase, methods camelCase, and constants UPPER_SNAKE_CASE. Lombok is available, but prefer explicit constructors in core domain models. Keep REST endpoints in `controller` packages and DTOs under `domain/.../dto`. Run formatters in your IDE and re-run SpotBugs if you touch generated code.

## Testing Guidelines
JUnit 5 and Spring Boot test starters provide the base harness. Follow the `FeatureNameTest` pattern (`TeamTest`, `UserTitleProviderTest`) and place reusable fixtures under `src/test/java/.../Fixture`. Use Mockito sparingly—prefer real collaborators or Spring slices for integration paths. Ensure new features extend coverage of critical branches and update JSON samples in `src/test/resources` when API contracts shift. Execute `./gradlew test` locally before opening a PR.

## Commit & Pull Request Guidelines
Match the existing convention `type #issue: summary` (for example, `feat #21: add ladder search API`). Squash minor fixups before submitting. Pull requests should describe motivation, list key changes, link the tracked issue, and attach screenshots or cURL examples for API changes. Confirm SpotBugs and tests pass, mention any migrations or config updates, and call out manual steps reviewers must run.
