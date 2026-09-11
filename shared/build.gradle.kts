plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinter)
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
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
