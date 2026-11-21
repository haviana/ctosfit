plugins {
    id("org.springframework.boot")
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":identity"))
    implementation(project(":box"))
    implementation(project(":exercise"))
    implementation(project(":programming"))
    implementation(project(":results"))
    implementation(project(":performance"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    archiveFileName.set("ctosfit.jar")
}
