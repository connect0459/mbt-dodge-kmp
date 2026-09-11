# Project Agents.md Guide

This is a [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) game whose logic is written in [MoonBit](https://docs.moonbitlang.com), compiled to `wasm`, and interpreted via [Chasm](https://github.com/CharlieTap/chasm) inside the KMP host. UI is native on each platform: SwiftUI on iOS, Android `View`s on Android.

The host architecture (MoonBit `wasm` guest → Chasm interpreter → native UI) is not this project's own invention — it reuses the shape validated by two sibling spikes: [`mbt-wasmkit-ios`](https://github.com/connect0459/mbt-wasmkit-ios) (WasmKit on iOS only) and [`mbt-chasm-kmp`](https://github.com/connect0459/mbt-chasm-kmp) (Chasm/KMP, verified on both iOS and Android with call-overhead measurements comfortably inside a modest 2D game's per-frame budget). This project is the first product built on that evidence, not another validation spike.

You can browse and install extra MoonBit agent skills here:
<https://github.com/moonbitlang/skills>

## Language Convention

This project may be released publicly. All of the following must be written in **English**:

- Commit messages
- Code comments
- Documentation (including `AGENTS.md`, `README.md`, etc.)
- Test names
- Error messages

## Project Structure

- **`guest/`** — a self-contained MoonBit module (its own `moon.mod`) compiled to `wasm`. Holds all game rules as pure functions (movement clamping, falling motion, collision detection, deterministic spawn positions, scoring) — this is the "logic" half of "logic in MoonBit, UI in Kotlin/Swift". Every exported function uses only scalar `Int` in/out or a `(Int, Int)` tuple return, mirroring the two call shapes `mbt-chasm-kmp` already measured; no new, unverified marshaling shape (raw `Bool`, collections, structs) is introduced.
- **`shared/`** — the KMP module that consumes `guest.wasm` via Chasm's build-time Kotlin binding generator. Also holds the Kotlin domain layer (`GameState`, `Block`, `GameEngine`) that composes per-tick calls into `GuestService` into one rich, immutable state transition — this layer is orchestration and state, not business rules; the rules themselves live in `guest/`. Targets `jvm`, `iosSimulatorArm64`, and `android`.
- **`iosApp/`** — a Tuist-managed Xcode project embedding `shared` as a Kotlin/Native framework (`Shared.framework`, via the `embedAndSignAppleFrameworkForXcode` direct-integration task). SwiftUI renders the game (`Canvas`) and drives the tick loop; it holds no game rules of its own.
- **`androidApp/`** — a plain `com.android.application` module depending on `shared` directly. A custom `View` renders the game and drives the tick loop; same no-game-rules boundary as `iosApp/`.
- **`docs/todo.md`** — the decision log: what's been verified, what broke, and why. Read it before changing the `guest`/`shared` boundary or the milestone scope.

## Coding convention

### MoonBit (`guest/`)

- MoonBit code is organized in block style, each block is separated by `///|`, the order of each block is irrelevant. In some refactorings, you can process block by block independently.
- Try to keep deprecated blocks in a file called `deprecated.mbt`.
- Keep the exported surface minimal and purpose-built for the game's actual rules — this is not a general-purpose FFI library.
- Every exported function must be a pure, deterministic transformation over scalars (or a `(Int, Int)` tuple return) — no hidden mutable state, no I/O.

### Kotlin (`shared/`, `iosApp/`, `androidApp/`)

- Follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).
- `kotlinter` is the formatter/linter (ktlint-gradle has an open bug excluding Chasm's generated sources; see `mbt-chasm-kmp/docs/todo.md` for the switch rationale).
- `GameState`/`Block` are immutable value objects; `GameEngine.tick` returns a new `GameState` rather than mutating one.

## Tooling

MoonBit tooling below applies only within `guest/` (run `cd guest && moon ...`, since `moon.mod` lives there, not at the repository root):

- `moon fmt` formats MoonBit source.
- `moon ide` provides project navigation helpers like `peek-def`, `outline`, and `find-references`. See $moonbit-agent-guide for details.
- `moon info` updates the generated interface (`.mbti`) of the package. If nothing in `.mbti` changes, the change has no visible effect on external consumers (here, Chasm's binding generator) — typically a safe refactoring.
- `moon test` runs tests. MoonBit supports snapshot testing; when changes affect outputs, run `moon test --update` to refresh snapshots.
- `moon test --enable-coverage` + `moon coverage analyze` checks `guest/`'s 100% coverage target (see docs/todo.md for why 100% was agreed for this layer specifically).
- In the last step of a `guest/` change, run `moon info && moon fmt` inside `guest/` and check the `.mbti` diff is expected.

For local quality verification:

- **pre-commit hooks** — catch formatting and lint issues on every commit (`pre-commit run --all-files` to run manually).
- **`just verify`** — MoonBit check across backends inside `guest/`; will grow a Kotlin/Gradle equivalent once `shared/` exists.

## Development Philosophy

### Red/Green TDD (Detroit school)

- Red → Green → Refactor cycle strictly followed
- Use real objects; mocks are only permitted at external boundaries (file system, external API, network). `GameEngine` tests use the real `GuestServiceImpl` backed by the compiled `guest.wasm` — the guest/host boundary is not an external boundary for this purpose
- Write tests BEFORE implementation; run tests AFTER implementation
- Agreed coverage targets: `guest/` (MoonBit) 100%; `shared/`'s Kotlin domain layer (`GameState`/`GameEngine`) covers its main paths, not exhaustively; `iosApp/`/`androidApp/` UI layers are verified manually (simulator/emulator screenshot), not by automated test — see `docs/todo.md`
- Exception: exploratory spikes may skip test-first with explicit user agreement; discard or rewrite the spike as a proper implementation afterward

### Domain Object Design

- Rich domain objects: pair data and logic in the same type
- Prefer immutability; avoid mutable state unless necessary
- Distinguish entities (identity-based) from value objects (value-based)
- Enforce layer boundaries through abstract types; no direct dependency on concrete implementations

### Evergreen Tests

- Test names describe WHAT business rule is being verified, not HOW
- Test names must not reference implementation details
- Test code serves as living documentation of the system's behavior

### Code Comments

- Do NOT write code comments unless explicitly permitted by the user
- Let the code speak for itself; let tests document the behavior
- Code = How, Tests = What, Commit messages = Why

## Git Conventions

### Format

```text
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

| Type | Description |
| :--- | :--- |
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `style` | Code style (formatting, whitespace) |
| `refactor` | Code change that is neither a fix nor a feature |
| `tidy` | Small, safe cleanup (< 2 min; no behavior change) |
| `test` | Adding or updating tests |
| `chore` | Build process, tooling, or config changes |
| `ci` | CI/CD pipeline changes (GitHub Actions, workflows) |
| `perf` | Performance improvement |

### Scopes

Scope is optional; use the area name when the change targets a specific part of the tree (e.g., `guest`, `shared`, `iosApp`, `androidApp`). Omit for project-wide changes.

### Type vs. Scope Precedence

The type vocabulary above mixes two axes: an **impact axis** (`feat`, `fix`, `perf`, `refactor` — the SemVer-relevant effect of a change) and a **domain axis** (`docs`, `style`, `test`, `chore`, `ci`, `tidy` — a layer with no runtime/SemVer effect). When a change is fully contained within a domain, use that domain as `type` (e.g. `docs: fix typo`); do not use it as `scope` on an impact-axis type (avoid `fix(docs): ...`). `scope` sub-divides whatever `type` already established (e.g. `feat(shared)`); it is not a substitute classification axis.

### Subject Line

- Use the imperative mood: "add", "fix", "remove" — not "added" or "adds"
- 72 characters max
- No trailing period

### Body (optional)

- Explain **why**, not what — the diff already shows what changed
- Leave one blank line between subject and body

### Footer (optional)

- `BREAKING CHANGE: <description>` for breaking changes
- `Closes #123` or `Fixes #456` to link issues

### Branch naming

`feat/xxx`, `fix/xxx`, `docs/xxx`
