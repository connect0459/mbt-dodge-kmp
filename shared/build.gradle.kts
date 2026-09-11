plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.chasm)
}

// Chasm's codegen output and our own generated GuestWasmBytes.kt aren't ours
// to style. ktlint-gradle can't exclude generated KMP sources (see
// mbt-chasm-kmp/docs/todo.md for the upstream bug); kotlinter's task-level
// exclude works instead.
tasks.withType<org.jmailen.gradle.kotlinter.tasks.ConfigurableKtLintTask>().configureEach {
    exclude { element -> element.file.path.contains("/generated/") }
}

chasm {
    modules {
        create("GuestService") {
            binary = layout.projectDirectory.file("src/commonMain/resources/guest.wasm")
            packageName = "dev.connect0459.mbtdodgekmp.shared.guest"
        }
    }
}

// Chasm's Gradle plugin only generates the interface/impl class shape from
// guest.wasm at build time; the raw bytes still have to be supplied by us at
// runtime (GuestServiceImpl's `binary` constructor parameter). Embedding them
// as a compiled-in constant works identically on every KMP target (jvm,
// iosSimulatorArm64, android) with no platform-specific resource/bundle
// lookup.
val guestWasmBytesDir = layout.buildDirectory.dir("generated/guestWasmBytes")

val generateGuestWasmBytes =
    tasks.register("generateGuestWasmBytes") {
        val wasmFile = layout.projectDirectory.file("src/commonMain/resources/guest.wasm")
        val outputDir = guestWasmBytesDir

        inputs.file(wasmFile)
        outputs.dir(outputDir)

        doLast {
            val packageDir = outputDir.get().dir("dev/connect0459/mbtdodgekmp/shared/guest").asFile
            packageDir.mkdirs()
            val bytes = wasmFile.asFile.readBytes()
            val literal = bytes.joinToString(", ") { it.toString() }
            packageDir.resolve("GuestWasmBytes.kt").writeText(
                """
                package dev.connect0459.mbtdodgekmp.shared.guest

                internal val GUEST_WASM_BYTES: ByteArray = byteArrayOf($literal)

                """.trimIndent(),
            )
        }
    }

kotlin {
    // Pins the JDK Gradle uses for jvm compilation regardless of the
    // invoking shell's JAVA_HOME.
    jvmToolchain(21)

    jvm()
    iosSimulatorArm64 {
        // Required for the `embedAndSignAppleFrameworkForXcode` Gradle task
        // (used by iosApp/'s Run Script build phase) to register at all.
        binaries.framework {
            baseName = "Shared"
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generateGuestWasmBytes.map { guestWasmBytesDir })
            dependencies {
                // Chasm's Gradle plugin only adds what its own generated code
                // needs to compile; using its lower-level embedding API
                // directly (module/store/instance/invoke/readInt, needed to
                // decode spawn_block's heap-boxed tuple return) requires this
                // runtime artifact as an explicit dependency.
                implementation(libs.chasm.runtime)
            }
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
