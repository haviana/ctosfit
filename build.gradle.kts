plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
}

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
        add("testImplementation", platform(libs.spring.boot.dependencies))
        add("testImplementation", libs.kotlin.test)
        add("testImplementation", libs.junit.jupiter)
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
