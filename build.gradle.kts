plugins {
    kotlin("jvm") version "2.1.20" apply false
    kotlin("plugin.spring") version "2.1.20" apply false
    id("org.springframework.boot") version "3.5.0" apply false
}

allprojects {
    group = "com.ctosfit"
    version = providers.gradleProperty("projectVersion").orElse("0.1.0-SNAPSHOT").get()
    repositories { mavenCentral() }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(21)
    }
    dependencies {
        "testImplementation"(kotlin("test"))
        "testImplementation"("org.junit.jupiter:junit-jupiter:5.12.2")
    }
    tasks.withType<Test>().configureEach { useJUnitPlatform() }
}
