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
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.client)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.starter.security.test)
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    archiveFileName.set("ctosfit.jar")
}
