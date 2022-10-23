package org.mider.produce.service.utlis

import io.ktor.server.application.*
import io.ktor.util.logging.*
import org.mider.produce.core.Configuration
import java.io.File

const val configPrefix = "service.produce"
const val isDebugging = true

fun getConfiguration(app: Application): Pair<Configuration, File> {

    val generatedWorkspace = app.environment.config.propertyOrNull("generatedDir")?.getString()?.let {
        val dir = File(it)
        if (!dir.exists()) throw Exception("")
        dir
    } ?: run {
        val dir = File(System.getProperty("user.dir"), if (isDebugging) "src/test/resources/generated" else "generated")
        if (!dir.exists()) dir.mkdir()
        dir
    }

    val tmpDir = app.environment.config.propertyOrNull("tmpDir")?.getString()?.let {
        val dir = File(it)
        if (!dir.exists()) throw Exception("")
        dir
    } ?: run {
        val dir = File(System.getProperty("user.dir"), if (isDebugging) "src/test/resources/tmp" else "tmp")
        if (!dir.exists()) dir.mkdir()
        dir
    }

    val cfg = Configuration(tmpDir)

    cfg.error = {
        when (it) {
            is Throwable -> app.log.error(it)
            else -> app.log.error(it.toString())
        }
    }

    cfg.info = {
        app.log.info(it.toString())
    }

    cfg.setupViaEnvironment(app.environment)

    return cfg to generatedWorkspace
}


fun Configuration.setupViaEnvironment(env: ApplicationEnvironment) {

    env.longOrDefault("commandTimeout") { commandTimeout = it }
    env.stringOrDefault("ffmpegConvertCommand") { ffmpegConvertCommand = it }
    env.stringOrDefault("timidityConvertCommand") { timidityConvertCommand = it }
    env.stringOrDefault("mscoreConvertMidi2Mp3Command") { mscoreConvertMidi2Mp3Command = it }
    env.stringOrDefault("mscoreConvertMidi2MSCZCommand") { mscoreConvertMidi2MSCZCommand = it }
    env.stringOrDefault("mscoreConvertMSCZ2PDFCommand") { mscoreConvertMSCZ2PDFCommand = it }
    env.stringOrDefault("mscoreConvertMSCZ2PNGSCommand") { mscoreConvertMSCZ2PNGSCommand = it }
    env.booleanOrDefault("debug") { debug = it }
}

private inline fun ApplicationEnvironment.stringOrDefault(path: String, block: (String) -> Unit) {
    config.propertyOrNull("$configPrefix.$path")?.getString()?.let(block)
}

private inline fun ApplicationEnvironment.intOrDefault(path: String, block: (Int) -> Unit) {
    config.propertyOrNull("$configPrefix.$path")?.getString()?.toIntOrNull()?.let(block)
}

private inline fun ApplicationEnvironment.longOrDefault(path: String, block: (Long) -> Unit) {
    config.propertyOrNull("$configPrefix.$path")?.getString()?.toLongOrNull()?.let(block)
}

private inline fun ApplicationEnvironment.booleanOrDefault(path: String, block: (Boolean) -> Unit) {
    config.propertyOrNull("$configPrefix.$path")?.getString()?.toBooleanStrictOrNull()?.let(block)
}