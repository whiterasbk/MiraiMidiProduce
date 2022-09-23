
plugins {
    val kotlinVersion = "1.6.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.10.3"
}

group = "bot.music.whiter"
version = "0.1.7"

repositories {
    maven("https://maven.aliyun.com/repository/central")
    mavenCentral()
    maven("https://jitpack.io")

    maven {
        url = uri("https://maven.pkg.github.com/mzdluo123/silk4j")

        credentials {
            val texts = File("github-package-token").readText().split(",")
            username = texts[0].trim() // 填写用户名
            password = texts[1].trim() // 填写 token
        }
    }
}

dependencies {
    api("com.github.nwaldispuehl:java-lame:v3.98.4")
    api("com.github.whiterasbk:mider:beta0.9.10")
    api("io.github.mzdluo123:silk4j:1.1-dev")
    api("org.apache.commons:commons-exec:1.3")
    api("com.belerweb:pinyin4j:2.5.1")

    // https://mvnrepository.com/artifact/io.ktor/ktor-client-core
    runtimeOnly("io.ktor:ktor-client-core:2.0.0")
}

//val _shadowJvmJar by tasks.creating(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) sd@{
//
//    from(project.configurations.findByName("jvmRuntimeClasspath"))
//}
