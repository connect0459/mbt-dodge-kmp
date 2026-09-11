# mbt-dodge-kmp

A small "dodge the falling blocks" game whose rules are written in [MoonBit](https://moonbitlang.com), compiled to `wasm`, and run inside a [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) host via [Chasm](https://github.com/CharlieTap/chasm) — with native UI on each platform (SwiftUI on iOS, Android `View`s on Android).

## Background

Two sibling spikes verified that this shape works and is fast enough:

- [`exp-mbt-wasmkit-ios`](https://github.com/connect0459/exp-mbt-wasmkit-ios) — MoonBit `wasm` → [WasmKit](https://github.com/swiftwasm/WasmKit) → iOS (iOS-only; WasmKit is a Swift-only runtime)
- [`exp-mbt-chasm-kmp`](https://github.com/connect0459/exp-mbt-chasm-kmp) — MoonBit `wasm` → Chasm → both iOS (Kotlin/Native) and Android (ART), with call-overhead measurements on every target comfortably inside a modest 2D game's per-frame budget

This project builds on `exp-mbt-chasm-kmp`'s host architecture — the only one of the two verified on both platforms — to ship an actual game rather than another feasibility spike.

## Project structure

- `guest/` — MoonBit module compiled to `wasm`; all game rules (movement, falling, collision, scoring, deterministic spawning) live here as pure functions
- `shared/` — KMP module consuming `guest.wasm` via Chasm's build-time binding generator, plus the Kotlin domain layer (`GameState`, `GameEngine`) that orchestrates per-tick calls into it; targets `jvm`, `iosSimulatorArm64`, and `android`
- `iosApp/` — Tuist-managed Xcode project embedding `shared` as a Kotlin/Native framework, rendering with SwiftUI
- `androidApp/` — Android application module depending on `shared` directly, rendering with a custom `View`

## Documentation

See [docs/todo.md](docs/todo.md) for the milestone plan and decision log — the project's primary record of what's been verified, what broke, and why.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

[Apache-2.0](LICENSE)
