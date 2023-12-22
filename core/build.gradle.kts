plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    implementation(libs.logback)
    compileOnly("com.fazecast:jSerialComm:2.10.4")
    testImplementation(libs.bundles.kotest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}
