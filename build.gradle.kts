
plugins {
    val kotlinVersion = "1.6.21"
    kotlin("jvm") version kotlinVersion apply false
    kotlin("plugin.serialization") version kotlinVersion apply false
    id("net.mamoe.mirai-console") version "2.12.3" apply false
}

allprojects {
    version = "0.1.8"
    group = "org.mider.produce"
}

subprojects {

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    repositories {
        maven("https://jitpack.io")
        maven("https://maven.aliyun.com/repository/central")
        mavenCentral()

        maven {
            url = uri("https://maven.pkg.github.com/mzdluo123/silk4j")
            credentials {
                val texts = File(rootDir.absoluteFile, "github-package-token").readText().split(",")
                username = texts[0].trim() // 填写用户名
                password = texts[1].trim() // 填写 token
            }
        }
    }

    //    repositories {
//
//        mavenCentral()
//
//
//        maven {
//
//        }
//    }
}

//java {
//    setSourceCompatibility(11)
//    setTargetCompatibility(11)
//}





//dependencies {
//    testImplementation(kotlin("test"))
//}
//
//tasks.test {
//    useJUnitPlatform()
//}
//
//tasks.withType<KotlinCompile> {
//    kotlinOptions.jvmTarget = "1.8"
//}