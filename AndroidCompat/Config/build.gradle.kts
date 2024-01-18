@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.kotlin.serialization.get().pluginId)
    id(libs.plugins.kotlinter.get().pluginId)
}

dependencies {
    // Shared
    implementation(libs.bundles.shared)
    testImplementation(libs.bundles.sharedTest)

    // AppDirs
    implementation(libs.appdirs) {
        exclude("net.java.dev.jna", "jna")
        exclude("net.java.dev.jna", "jna-platform")
    }
}