
dependencies {
    implementation("com.github.nwaldispuehl:java-lame:v3.98.4")
    api("com.github.whiterasbk:mider:beta0.9.14")
    api("io.github.mzdluo123:silk4j:1.1-dev")
    implementation("org.apache.commons:commons-exec:1.3")
    api("com.belerweb:pinyin4j:2.5.1")

    api("io.ktor:ktor-client-core:2.0.0")
    api("io.ktor:ktor-client-okhttp:2.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}