
dependencies {
    implementation("com.github.nwaldispuehl:java-lame:v3.98.4")
    implementation("com.github.whiterasbk:mider:beta0.9.14")
    implementation("io.github.mzdluo123:silk4j:1.1-dev")
    implementation("org.apache.commons:commons-exec:1.3")
    implementation("com.belerweb:pinyin4j:2.5.1")

    api("io.ktor:ktor-client-core:2.1.1")
    api("io.ktor:ktor-client-okhttp:2.1.1")


//    implementation("io.ktor:ktor-client-core:2.1.2")
//    implementation("io.ktor:ktor-client-core-jvm:2.1.2")

//    implementation("io.ktor:ktor-client-okhttp:2.1.2")
    // https://mvnrepository.com/artifact/io.ktor/ktor-client-okhttp

//    // https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
//    implementation("com.squareup.okhttp3:okhttp:4.10.0")


    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

}

//plugins {
//    id("java")
//}
//
//group = "org.example"
//version = "1.0-SNAPSHOT"
//
//repositories {
//    mavenCentral()
//}
//
//dependencies {

//}
//
tasks.getByName<Test>("test") {
    useJUnitPlatform()
}