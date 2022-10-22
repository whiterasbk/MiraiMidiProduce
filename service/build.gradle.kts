val ktorVersion: String = "2.1.2"
val kotlinVersion: String = "1.6.21"
val logbackVersion: String = "1.4.4"

plugins {
    application
}

apply(plugin = "io.ktor.plugin")
apply(plugin = "application")

application {
    mainClass.set("org.mider.produce.service.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}