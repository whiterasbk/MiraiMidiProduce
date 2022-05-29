
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

    maven {
        url = uri("https://maven.pkg.github.com/mzdluo123/silk4j")

        credentials {
            username = "whiterasbk" // 填写用户名
            password = File("github-package-token").readText() // 填写token
        }
    }
}

dependencies {
    implementation("com.github.nwaldispuehl:java-lame:v3.98.4")
    implementation("com.github.whiterasbk:mider:beta0.9.2")
    implementation("io.github.mzdluo123:silk4j:1.1-dev")
    implementation("org.apache.commons:commons-exec:1.3")
}

//val _shadowJvmJar by tasks.creating(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) sd@{
//
//    from(project.configurations.findByName("jvmRuntimeClasspath"))
//}
