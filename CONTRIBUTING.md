# Contributing

## Prerequisites

- [MoonBit toolchain](https://www.moonbitlang.com/download/) — `moon` CLI, for `guest/`
- [just](https://just.systems/) — task runner
- [pre-commit](https://pre-commit.com/) — hook runner
- JDK + Gradle — for `shared/`
- [Xcode](https://developer.apple.com/xcode/) and [Tuist](https://tuist.dev/) — for the `iosApp/` host
- [Android SDK](https://developer.android.com/studio) (platform 36, an `arm64-v8a` emulator image) — for the `androidApp/` host

## Setup

```sh
git clone https://github.com/connect0459/mbt-dodge-kmp
cd mbt-dodge-kmp
just setup
```

`just setup` runs `moon update` inside `guest/` to fetch package dependencies, and installs the pre-commit hooks (`pre-commit install`).

### pre-commit hooks

To run all hooks manually:

```sh
pre-commit run --all-files
```

## Project structure

- `guest/` — a self-contained MoonBit module (its own `moon.mod`), compiled to `wasm`. Holds every game rule as a pure function.
- `shared/` — the KMP module consuming `guest.wasm` via Chasm's build-time Kotlin binding generator, plus the Kotlin domain layer (`GameState`, `GameEngine`) that composes per-tick calls into it. Targets `jvm`, `iosSimulatorArm64`, and `android`.
- `iosApp/` — the Tuist-managed Xcode project embedding `shared` as a Kotlin/Native framework.
- `androidApp/` — a plain `com.android.application` module depending on `shared` as a regular Gradle project dependency.
- `docs/todo.md` — the log of what's been verified, what broke, and why. Read it before changing the `guest`/`shared` boundary.

## Development workflow

| Command | Purpose |
| :--- | :--- |
| `cd guest && moon test` | Run all `guest/` tests |
| `cd guest && moon test --target wasm` | Run `guest/` tests on a specific backend |
| `cd guest && moon fmt` | Format `guest/` source files |
| `cd guest && moon check` | Type-check `guest/` without building |
| `cd guest && moon info` | Regenerate `guest/`'s `.mbti` interface file |
| `cd guest && moon test --enable-coverage && moon coverage analyze` | Check `guest/`'s 100% coverage target |
| `just verify` | Run the full `guest/` CI-equivalent check locally |
| `./gradlew lintKotlin` / `formatKotlin` | Lint / auto-format Kotlin sources across all modules |
| `./gradlew :shared:jvmTest :shared:iosSimulatorArm64Test` | Run `shared/` tests on the fast (non-device) targets |
| `just ios-generate` | `build-guest-wasm`, then generate the Tuist-managed `iosApp/` Xcode project |
| `./gradlew :androidApp:installDebug` | Build and install `androidApp/` on a connected Android emulator/device |

Before opening a pull request touching `guest/`, run:

```sh
just verify
```

This mirrors the CI matrix: it checks all four backends (`js`, `wasm`, `wasm-gc`, `native`) for `guest/`.

## Testing guidelines

This project follows **Red → Green → Refactor** (Detroit-school TDD):

- Write a failing test first, then implement.
- Use real objects; mocks are only permitted at external boundaries. `GameEngine`'s tests run against the real, compiled `guest.wasm` via `GuestServiceImpl` — the guest/host boundary is not treated as external here.
- Test names describe **what business rule** is verified, not how.
- Coverage targets: `guest/` 100%; `shared/`'s Kotlin domain layer covers its main paths; `iosApp/`/`androidApp/` UI is verified manually, not by automated test.
- Exception: exploratory spikes may skip test-first with explicit agreement — discard or rewrite as a proper implementation afterward.

## Commit format

```text
<type>(<scope>): <subject>
```

**Types**: `feat`, `fix`, `docs`, `style`, `refactor`, `tidy`, `test`, `chore`, `ci`, `perf`

**Scope**: area name when the change targets one specific part of the tree (`guest`, `shared`, `iosApp`, `androidApp`); omit for project-wide changes.

**Subject**: imperative mood, 72 characters max, no trailing period.

Examples:

```text
feat(guest): add collision detection between the player and a block
fix(shared): clamp player position before checking for a collision
docs: record Milestone 3 GameEngine test results
```

## Pull request process

1. Fork the repository and create a branch: `feat/xxx`, `fix/xxx`, `docs/xxx`.
2. Follow the Red → Green → Refactor cycle for `guest/` and `shared/` domain-layer changes.
3. Run `just verify` and commit any resulting diffs.
4. If the change touches `guest/`'s exported API, run `moon info` (inside `guest/`) and verify the `.mbti` diff is expected, and confirm `moon coverage analyze` still reports full coverage.
5. If the change affects `shared/`/`iosApp/`/`androidApp/`, confirm the relevant platform target still builds and runs — `./gradlew :shared:jvmTest :shared:iosSimulatorArm64Test`, and/or a simulator run via `just ios-generate` or an emulator run via `./gradlew :androidApp:installDebug`.
6. Update `docs/todo.md` if the change resolves an open question or surfaces a new one — this file is the project's primary record, more so than commit messages alone.
7. Open a pull request.

## Code style

- No code comments unless the **why** is genuinely non-obvious.
- Prefer immutability; avoid mutable state unless necessary.
- Keep `guest/`'s exported surface limited to the game's actual rules — this is not a general-purpose FFI library.
- All user-facing strings (test names, error messages, doc comments) must be in **English**.
