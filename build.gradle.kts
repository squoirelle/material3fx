// Minimum supported Java version: class files target it whatever JDK builds them.
val minimumJava = 21
// -Pjdk=21 builds and tests with another JDK (default: 25); see also -Pjavafx in the modules.
val buildJdk = providers.gradleProperty("jdk").map { it.toInt() }.getOrElse(25)

subprojects {
    apply(plugin = "java-library")

    group = "io.github.squoirelle"
    version = "0.2.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(buildJdk)
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = minimumJava
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-serial", "-Xlint:-this-escape", "-Xlint:-requires-transitive-automatic"))
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }
}
