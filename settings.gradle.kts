plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "KtFirmata"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

buildCache {
    local {
        directory = File(rootDir, ".cache")
        removeUnusedEntriesAfterDays = 30
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("hivemq", "com.hivemq:hivemq-mqtt-client:1.3.3")
            library("jSerialComm", "com.fazecast:jSerialComm:2.10.4")
            library("logback", "ch.qos.logback:logback-classic:1.4.14")
            library("kotest-assertions-core", "io.kotest:kotest-assertions-core:5.8.0")
            library("kotest-runner-junit5", "io.kotest:kotest-runner-junit5:5.8.0")
            bundle("kotest", listOf("kotest-assertions-core", "kotest-runner-junit5"))
        }
    }
}

include(":core")
include(":server")
