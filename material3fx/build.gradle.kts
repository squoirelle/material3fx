plugins {
    alias(libs.plugins.javafx)
    `maven-publish`
    signing
}

description = "Material Design 3 for JavaFX: themes, a stylesheet for the standard controls, and Material components."

javafx {
    // -Pjavafx=21.0.13 builds and tests against another JavaFX release.
    version = providers.gradleProperty("javafx").getOrElse(libs.versions.javafxMin.get())
    modules("javafx.controls")
    // Not in the published POM: applications bring their own JavaFX, for their own platforms.
    configurations = arrayOf("compileOnly", "testImplementation")
}

java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    jvmArgs("--enable-native-access=javafx.graphics", "--enable-native-access=ALL-UNNAMED")
}

tasks.withType<Jar>().configureEach {
    from(rootProject.files("LICENSE", "NOTICE")) {
        into("META-INF")
    }
}

tasks.named<Javadoc>("javadoc") {
    (options as StandardJavadocDocletOptions).apply {
        encoding = "UTF-8"
        docTitle = "Material3FX ${project.version}"
        windowTitle = "Material3FX ${project.version}"
        links("https://docs.oracle.com/en/java/javase/21/docs/api/", "https://openjfx.io/javadoc/21/")
        addBooleanOption("Xdoclint:all,-missing", true)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "material3fx"
            from(components["java"])
            pom {
                name = "Material3FX"
                description = project.description
                url = "https://github.com/squoirelle/material3fx"
                licenses {
                    license {
                        name = "Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                        distribution = "repo"
                    }
                }
                developers {
                    developer {
                        id = "squoirelle"
                        name = "Lilian Stefanuto"
                        url = "https://github.com/squoirelle"
                    }
                }
                scm {
                    url = "https://github.com/squoirelle/material3fx"
                    connection = "scm:git:https://github.com/squoirelle/material3fx.git"
                    developerConnection = "scm:git:ssh://git@github.com/squoirelle/material3fx.git"
                }
                issueManagement {
                    system = "GitHub"
                    url = "https://github.com/squoirelle/material3fx/issues"
                }
            }
        }
    }
    repositories {
        // ./gradlew publishAllPublicationsToStagingRepository writes a Maven Central bundle here.
        maven {
            name = "staging"
            url = uri(rootProject.layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

signing {
    // Signs when a key is given: -PsigningKey=<ascii-armored key> -PsigningPassword=... (or
    // ORG_GRADLE_PROJECT_signingKey / ORG_GRADLE_PROJECT_signingPassword in CI).
    val key = providers.gradleProperty("signingKey").orNull
    if (key != null) {
        useInMemoryPgpKeys(key, providers.gradleProperty("signingPassword").orNull)
        sign(publishing.publications["maven"])
    }
}
