dependencies {
    implementation(platform(libs.spring.boot.dependencies))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.jackson3.module.kotlin)

    testImplementation(libs.spring.boot.starter.test)
}
