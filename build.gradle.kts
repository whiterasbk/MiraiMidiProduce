plugins {
    val kotlinVersion = "1.6.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.10.3"
}

group = "bot.music.whiter"
version = "pre-0.1.4"

repositories {
    maven("https://maven.aliyun.com/repository/central")
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.nwaldispuehl:java-lame:v3.98.4")
    implementation("com.github.whiterasbk:mider:beta0.9.2")
}

//val _shadowJvmJar by tasks.creating(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) sd@{
//
//    from(project.configurations.findByName("jvmRuntimeClasspath"))
//}
