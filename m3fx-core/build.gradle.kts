plugins {
    alias(libs.plugins.javafx)
}

javafx {
    // -Pjavafx=21.0.13 builds and tests against another JavaFX release.
    version = providers.gradleProperty("javafx").getOrElse(libs.versions.javafxMin.get())
    modules("javafx.controls")
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    jvmArgs("--enable-native-access=javafx.graphics", "--enable-native-access=ALL-UNNAMED")
}
