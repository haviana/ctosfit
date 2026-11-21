plugins {
    id("org.springframework.boot")
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    // Spring Boot's Gradle plugin does not automatically apply dependency management.
    // Import the Boot BOM so starters and Jackson modules receive compatible versions.
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.0"))
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.0"))

    implementation(project(":identity"))
    implementation(project(":box"))
    implementation(project(":exercise"))
    implementation(project(":programming"))
    implementation(project(":results"))
    implementation(project(":performance"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(kotlin("reflect"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    archiveFileName.set("ctosfit.jar")
}
