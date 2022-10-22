val ktor_version: String = "2.1.2"
val kotlin_version: String = "1.6.21"
val logback_version: String = "1.4.4"

plugins {
    application
//    kotlin("jvm") version "1.7.20"
}

apply(plugin = "io.ktor.plugin")
apply(plugin = "application")


application {
    mainClass.set("org.mider.produce.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}