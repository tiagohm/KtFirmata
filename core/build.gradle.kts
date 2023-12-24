plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    api(libs.apache.commons)
    implementation(libs.logback)
    compileOnly(libs.jSerialComm)
    testImplementation(libs.jSerialComm)
    testImplementation(libs.bundles.kotest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
