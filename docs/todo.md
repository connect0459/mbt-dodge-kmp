# todo - mbt-dodge-kmp

Current state: **Milestone 0 (scaffold) and Milestone 1 (`guest/` game rules) are complete.** All seven exported functions are implemented, TDD'd, and pass on all four MoonBit backends (`js`, `wasm`, `wasm-gc`, `native`) with 100% line coverage. `shared/` (Chasm wiring), the Kotlin domain layer, Android target, and both native UIs are not yet built.

---

## Background

This project builds an actual game on top of two sibling spikes' validated results, rather than running another feasibility spike of its own:

- [`mbt-wasmkit-ios`](https://github.com/connect0459/mbt-wasmkit-ios) verified MoonBit `wasm` → [WasmKit](https://github.com/swiftwasm/WasmKit) → iOS, but WasmKit is Swift-only, so this route has no Android story.
- [`mbt-chasm-kmp`](https://github.com/connect0459/mbt-chasm-kmp) verified MoonBit `wasm` → [Chasm](https://github.com/CharlieTap/chasm) → both iOS (Kotlin/Native) and Android (ART), with call-overhead measurements (scalar calls and heap-boxed-tuple returns) on every target well inside a modest 2D game's per-frame budget.

This project adopts `mbt-chasm-kmp`'s host architecture as-is (it is the only one of the two verified on both platforms) and reuses its scaffold pattern (Chasm Gradle plugin config, `generateGuestWasmBytes` embedding task, `kotlinter`, Tuist direct-integration for iOS) directly rather than re-deriving it.

An earlier proposal — following the dev.to article ["Build a Mobile Game with MoonBit"](https://dev.to/moonbitlang/build-a-mobile-game-with-moonbit-364i), which cross-compiles MoonBit to a `.so` via the Android NDK and Raylib under `NativeActivity` — was considered and rejected before any code was written. Two independent reasons: (1) `mbt-wasmkit-ios/docs/todo.md` Milestone 1 already found that MoonBit's C-emission path, which that article's scaffold depends on, no longer exists in the current toolchain (removed in the 2025-03 LLVM-backend migration); (2) the article's approach works specifically by having Raylib bypass the native UI toolkit entirely (`NativeActivity` never touches the Android View hierarchy), which is the opposite of this project's actual goal — logic in MoonBit, UI in Kotlin/Swift's native toolkits. The two verified spikes above already demonstrate the correct alternative: MoonBit `wasm` → host-side interpreter → native UI, with native UI intact.

## Decisions made before any code existed

- **Game choice: dodge the falling blocks**, not a Flappy-Bird-style tap game. Both were reachable using only the two call shapes already measured by `mbt-chasm-kmp` (scalar `Int` in/out, and a `move_point`-shaped `(Int, Int)` tuple return); falling-blocks was chosen by the user over the alternatives.
- **No `Bool` marshaling across the wasm boundary.** All guest-exported predicates (`is_collision`, `is_off_screen`, `should_spawn`) return `Int` (`0`/`1`) instead of `Bool`, since neither sibling project exercised passing or returning a MoonBit `Bool` through Chasm's codegen — reusing the already-verified scalar-`Int` shape avoids opening an unverified marshaling question this project doesn't need to answer.
- **Coverage targets, agreed with the user before implementation**: `guest/` (MoonBit) 100% — realistic since it is a small set of pure functions; `shared/`'s Kotlin domain layer (`GameState`/`GameEngine`) covers its main paths, not exhaustively; `iosApp/`/`androidApp/` UI layers are verified manually (simulator/emulator screenshot), matching both sibling projects' own choice not to carry an automated UI/Xcode CI job.
- **Milestone ordering** deliberately defers the Android KMP target (and `androidApp/`) to Milestone 4, after `guest/`, Chasm wiring, and the Kotlin domain layer all exist and are tested on `jvm`/`iosSimulatorArm64` — mirrors `mbt-chasm-kmp`'s own milestone split (Milestone 1: iOS-capable scaffold; Milestone 2: Android target added on top) rather than standing up every target at once.

## Milestone 0: Project scaffold

- [x] Ported `AGENTS.md`/`CLAUDE.md`, `.markdownlint.json`, `.pre-commit-config.yaml`, `apm.yml`, `.github/` (CI, Copilot setup, issue/PR templates), `justfile`, `LICENSE`, `CODE_OF_CONDUCT.md`, `CONTRIBUTING.md`, `SECURITY.md`, `README.md` from `mbt-chasm-kmp`, adapting each for this project's actual purpose (a shipped game, not a validation spike) and narrower initial scope (no `androidApp/`/Android target yet; added in Milestone 4)
- [x] `settings.gradle.kts` + root `build.gradle.kts` (group `dev.connect0459.mbtdodgekmp`) + `gradle/libs.versions.toml` (Kotlin 2.4.20, `kotlinter`, `chasm` — matching `mbt-chasm-kmp`'s pinned versions), `shared/` module not yet populated (Milestone 2)
- [x] Scaffolded `guest/` via `moon new --user connect0459 --name mbt_dodge_kmp_guest guest`, then stripped what didn't fit a single-purpose guest module: the nested `.git`/`.githooks` (the repository root is the single source of truth for version control), `.github/` (CI lives at the repository root only), `cmd/main` (no standalone executable needed), `AGENTS.md`/`LICENSE`/`README.mbt.md` (the repository root already carries these) — mirrors `mbt-chasm-kmp/guest`'s own convention
- [x] `guest/moon.pkg`: `pkgtype(kind: "foreign_library")` + `options(link: { "wasm": { "export-memory-name": "memory" } })`, identical to `mbt-chasm-kmp/guest` (the memory export is needed again here for `spawn_block`'s tuple return, exactly as it was for that project's `move_point`)
- [ ] `git init` and initial commit (pending — done after this milestone's files are all in place)
- [ ] Create the GitHub remote and push — deliberately **not** done as part of this plan; a separate, explicit user decision (see this repository's own planning conversation)

## Milestone 1: `guest/` game rules

Goal: implement every game rule as a pure, deterministic MoonBit function, using only the two call shapes `mbt-chasm-kmp` already measured (scalar `Int` in/out; a `(Int, Int)` tuple return for `spawn_block`, mirroring `move_point`'s heap-boxed-pointer shape exactly).

- [x] TDD (Red → Green) for all seven exports, in `guest_test.mbt` first:
  - `move_player(x, dx, min_x, max_x) -> Int` — clamps position to `[min_x, max_x]`
  - `fall_step(y, speed) -> Int` — advances a block's `y` by `speed`
  - `spawn_block(seed, min_x, max_x) -> (Int, Int)` — advances an internal LCG (`(seed * 1103515245 + 12345) & 0x7fffffff`) and derives an `x` in range from it, returning `(x, next_seed)`
  - `is_collision(player_x, player_width, block_x, block_size, player_y, block_y, hit_margin) -> Int` — `1`/`0`, checks horizontal-range overlap AND vertical distance within `hit_margin`
  - `is_off_screen(block_y, screen_height) -> Int` — `1`/`0`
  - `should_spawn(tick_count, interval) -> Int` — `1`/`0`, fires when `tick_count % interval == 0`
  - `increment_score(score) -> Int`
- [x] `spawn_block`'s tests are property-based (`x` stays in range; same seed reproduces the same `(x, next_seed)`; seed changes on every call; different seeds diverge) rather than hand-computed magic numbers — the LCG's exact output depends on `Int` overflow behavior that wasn't worth hand-deriving when `moon test` can just be run directly
- [x] `moon test --target wasm` (Red, then Green), then `moon test --target all` (`js`/`wasm`/`wasm-gc`/`native`) — 19 tests pass on every backend
- [x] `moon test --enable-coverage && moon coverage analyze` — initially found one uncovered branch in `is_collision` (the `block_y > player_y` arm of its vertical-distance check was never exercised); added a test where the block is below the player within margin, then confirmed **all source files are fully covered**
- [x] `moon check --deny-warn --target all`, `moon info` (regenerated `pkg.generated.mbti` — all seven functions listed with the expected signatures), `moon fmt`

## Milestone 2: `shared/` Chasm wiring (not started)

Goal: wire `guest.wasm` into a KMP `shared` module via Chasm, same as `mbt-chasm-kmp`'s `GuestServiceFactory`/`generateGuestWasmBytes` pattern, and verify each of the seven exports round-trips correctly from Kotlin on `jvm` and `iosSimulatorArm64`.

- [ ] Not started

## Milestone 3: Kotlin domain layer (not started)

Goal: `Block`/`GameState`/`GameEngine.tick` in `shared/src/commonMain`, composing per-tick `GuestService` calls into one immutable state transition. TDD against the real `GuestServiceImpl` (Detroit school — the guest/host boundary is not treated as external here).

- [ ] Not started

## Milestone 4: Android target (not started)

- [ ] Not started

## Milestone 5: iOS UI (not started)

## Milestone 6: Android UI (not started)

## Milestone 7: Call-overhead re-measurement for the real per-tick call pattern (not started)

## Open questions

- [ ] None yet — will record here as they come up in Milestone 2 onward.
