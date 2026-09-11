plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinter) apply false
    alias(libs.plugins.chasm) apply false
}

allprojects {
    group = "dev.connect0459.mbtdodgekmp"
    version = "0.1.0"
}
