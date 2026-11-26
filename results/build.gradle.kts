plugins { kotlin("jvm") }

dependencies {
    implementation(platform(libs.spring.boot.dependencies))
    implementation(libs.spring.boot.starter.web)
}

java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }
