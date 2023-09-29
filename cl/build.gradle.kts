
plugins {
    application
}

apply(plugin = "io.ktor.plugin")
apply(plugin = "application")

application {
    mainClass.set("org.mider.produce.cl.ClKt")
}

dependencies {
    implementation(project(":core"))

    implementation("info.picocli:picocli:4.7.5")
    implementation("com.github.nwaldispuehl:java-lame:v3.98.4")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.named<Jar>("jar") {

    manifest {
        attributes("Main-Class" to "org.mider.produce.cl.ClKt")
    }

    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}