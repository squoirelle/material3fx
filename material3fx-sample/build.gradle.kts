plugins {
    alias(libs.plugins.javafx)
    application
}

javafx {
    // -Pjavafx=21.0.13 builds and tests against another JavaFX release.
    version = providers.gradleProperty("javafx").getOrElse(libs.versions.javafx.get())
    modules("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation(project(":material3fx"))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    // An application module: its public types need not re-export JavaFX or declare constructors.
    options.compilerArgs.addAll(listOf("-Xlint:-exports", "-Xlint:-missing-explicit-ctor"))
}

tasks.named<Test>("test") {
    jvmArgs("--enable-native-access=javafx.graphics", "--enable-native-access=ALL-UNNAMED")
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("--enable-native-access=javafx.graphics")
    // Vsync off on Linux, as in the demo: NVIDIA under XWayland shows black frames otherwise.
    val linux = System.getProperty("os.name").lowercase().contains("linux")
    val vsync = providers.gradleProperty("vsync").orNull ?: if (linux) "false" else null
    vsync?.let {
        jvmArgs("-Dprism.vsync=$it")
        if (it == "false") environment("__GL_SYNC_TO_VBLANK", "0")
    }
}

application {
    mainModule.set("io.github.squoirelle.material3fx.sample")
    mainClass.set("io.github.squoirelle.material3fx.sample.StockroomApp")
}
