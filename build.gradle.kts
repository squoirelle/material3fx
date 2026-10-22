subprojects {
    apply(plugin = "java-library")

    group = "dev.m3fx"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-serial", "-Xlint:-this-escape", "-Xlint:-requires-transitive-automatic"))
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }
}
