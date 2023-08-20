
plugins {
    val kotlinVersion = "1.6.21"
    kotlin("jvm") version kotlinVersion apply false
    kotlin("plugin.serialization") version kotlinVersion apply false
    id("net.mamoe.mirai-console") version "2.12.3" apply false
    id("io.ktor.plugin") version "2.1.2" apply false
}

allprojects {
    version = "0.1.9"
    group = "org.mider.produce"
}

subprojects {

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.aliyun.com/repository/central")

        maven {
            url = uri("https://maven.pkg.github.com/mzdluo123/silk4j")
            credentials {

                val file = File(rootDir.absoluteFile, "github-package-token")

                if (!file.exists()) {

                    val env_token: String? = System.getenv("GITHUB_TOKEN")
                    val env_user: String? = System.getenv("GITHUB_USER")

                    if (env_user == null || env_token == null)
                        throw Exception("please provide github-package-token with token,name inside in root path")
                    username = env_user
                    password = env_token
                    logger.log(LogLevel.INFO, "file github-package-token not found, found env token.")
                } else {
                    val texts = file.readText().split(",")
                    username = texts[0].trim() // 填写用户名
                    password = texts[1].trim() // 填写 token
                }
            }
        }
    }

}