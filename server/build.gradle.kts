import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm")
    id("maven-publish")
    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation(libs.hivemq)
    implementation(libs.jSerialComm)
    implementation(libs.harawata)
    implementation(libs.logback)
    testImplementation(libs.bundles.kotest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
        }
    }
}

tasks.withType<BootJar> {
    archiveFileName = "kt-firmata-server.jar"
    destinationDirectory = file("$rootDir")

    manifest {
        attributes["Start-Class"] = "kt.firmata.server.MainKt"
    }
}
