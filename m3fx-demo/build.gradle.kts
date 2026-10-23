plugins {
    alias(libs.plugins.javafx)
    application
}

javafx {
    // -Pjavafx=21.0.13 builds and tests against another JavaFX release.
    version = providers.gradleProperty("javafx").getOrElse(libs.versions.javafx.get())
    modules("javafx.controls", "javafx.swing")
}

dependencies {
    implementation(project(":m3fx-core"))
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xlint:-exports", "-Xlint:-missing-explicit-ctor"))
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("--enable-native-access=javafx.graphics")
    // -Pprism=sw runs with the software renderer, to tell GPU/driver issues from library ones.
    providers.gradleProperty("prism").orNull?.let { jvmArgs("-Dprism.order=$it", "-Dprism.verbose=true") }
    // -Pdirtyopts=false repaints whole frames instead of only the changed regions.
    providers.gradleProperty("dirtyopts").orNull?.let { jvmArgs("-Dprism.dirtyopts=$it") }
    // Vsync is off by default on Linux: with NVIDIA under XWayland (e.g. COSMIC), vsynced buffer
    // swaps show black frames during animations. -Pvsync=true turns it back on.
    val linux = System.getProperty("os.name").lowercase().contains("linux")
    val vsync = providers.gradleProperty("vsync").orNull ?: if (linux) "false" else null
    vsync?.let {
        jvmArgs("-Dprism.vsync=$it")
        if (it == "false") environment("__GL_SYNC_TO_VBLANK", "0")
    }
}

application {
    mainModule.set("dev.m3fx.demo")
    mainClass.set("dev.m3fx.demo.DemoApp")
}

tasks.register<JavaExec>("screenshots") {
    group = "verification"
    description = "Renders every demo page to PNG files in build/screenshots."
    classpath = sourceSets["main"].runtimeClasspath
    mainModule.set("dev.m3fx.demo")
    mainClass.set("dev.m3fx.demo.Screenshots")
    args(layout.buildDirectory.dir("screenshots").get().asFile.absolutePath)
    providers.gradleProperty("page").orNull?.let { args(it) }
}
