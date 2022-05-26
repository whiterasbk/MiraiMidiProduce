plugins {
    val kotlinVersion = "1.6.21"
    kotlin("jvm") version kotlinVersion // '1.5.30'
    kotlin("plugin.serialization") version kotlinVersion // '1.5.30'
    id("net.mamoe.mirai-console") version "2.10.3"
}

group = "bot.music.whiter"
version = "0.1.3"

repositories {
    maven("https://maven.aliyun.com/repository/central")
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.nwaldispuehl:java-lame:v3.98.4")
    implementation("com.github.whiterasbk:mider:kbeta0.9.1")
}
