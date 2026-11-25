plugins { kotlin("jvm") }

java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }

dependencies {
    implementation(platform(libs.spring.boot.dependencies))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.jackson3.module.kotlin)
}
