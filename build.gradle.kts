import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
}

// Resolve the catalog explicitly so it can be reused from subproject configuration.
val dependencyCatalog = extensions
    .getByType<VersionCatalogsExtension>()
    .named("libs")

val springBootBom = dependencyCatalog.findLibrary("spring-boot-dependencies").get()
val kotlinTest = dependencyCatalog.findLibrary("kotlin-test").get()
val junitJupiter = dependencyCatalog.findLibrary("junit-jupiter").get()

allprojects {
    group = "com.ctosfit"
    version = providers.gradleProperty("projectVersion").orElse("0.1.0-SNAPSHOT").get()
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(25)
    }

    dependencies {
        // Align test libraries consistently across every module with the Spring Boot BOM.
        add("testImplementation", platform(springBootBom))
        add("testImplementation", kotlinTest)
        add("testImplementation", junitJupiter)
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform {
            val include = providers.gradleProperty("includeTags").orNull
            if (!include.isNullOrBlank()) {
                includeTags(*include.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toTypedArray())
            }
        }
    }
}
