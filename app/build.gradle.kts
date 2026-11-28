plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    // Import the Spring Boot BOM for application runtime dependencies.
    implementation(platform(libs.spring.boot.dependencies))

    implementation(project(":identity"))
    implementation(project(":box"))
    implementation(project(":exercise"))
    implementation(project(":programming"))
    implementation(project(":results"))
    implementation(project(":performance"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.jdbc)
    implementation(libs.spring.boot.starter.flyway)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.client)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)

    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.starter.security.test)
    testImplementation(libs.h2)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    archiveFileName.set("ctosfit.jar")
}

tasks.named<Test>("test") {
    val filtered = providers.gradleProperty("includeTags").orNull?.isNotBlank() == true
    if (filtered) {
        afterSuite(
            org.gradle.kotlin.dsl.KotlinClosure2<
                org.gradle.api.tasks.testing.TestDescriptor,
                org.gradle.api.tasks.testing.TestResult,
                Unit,
            >({ descriptor, result ->
                if (descriptor.parent == null && result.testCount == 0L) {
                    throw GradleException("includeTags matched no tests in :app")
                }
            }),
        )
    }
}
