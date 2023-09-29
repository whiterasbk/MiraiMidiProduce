package org.mider.produce.cl

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import picocli.CommandLine
import java.util.*
import javax.sound.midi.MidiSystem
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        // interactive mode
        val cmdRegex = Regex(
            ">(g|f|\\d+b)((;[-+b#]?[A-G](min|maj|major|minor)?)|(;\\d)|" +
                    "(;img)|(;pdf)|(;mscz)|(;sing(:[a-zA-Z-]{2,4})?(:[fm]?\\d+)?)|" +
                    "(;midi)|(;\\d{1,3}%)|(;/\\d+)|(;\\d+dB)|(;[↑↓]+)|(;\\d+(\\.\\d+)?x)|" +
                    "(;i=([a-zA-Z-]+|\\d+))|(;\\d/\\d))*>"
        )

        val (cfg, tmp) = getConfiguration()
        cfg.info = {}
        cfg.error = {
            errorPrintln(it.toString())
        }

        tmp.deleteOnExit()

        runBlocking {
            val scanner = Scanner(System.`in`)
            var currentTrackHead = ">g>"
            var proxy: String? = null
            val helpMessage = "[mider-info] enter exit to quit, stop to stop current midi sequencer"
            val sequencer = MidiSystem.getSequencer()

            sequencer.use { seq ->
                while (true) {
                    print("$currentTrackHead ")
                    val line = scanner.nextLine().trim()
                    when {
                        line == "exit" -> exitProcess(0)
                        line == "help" -> infoPrint("$helpMessage\n")
                        line == "stop" -> seq.stop()
                        line == "" -> continue
                        line matches cmdRegex -> {
                            currentTrackHead = line
                        }
                        line.startsWith("set ") -> {
                            val attr = line.removePrefix("set ").split("=")
                            if (attr.size == 2 && attr.first() == "proxy") {
                                proxy = attr[1]
                            } else errorPrintln("[mider-error] wrongly setting attribute")
                        }

                        else -> {
                            try {
                                val (result, stream) = cfg.generate(
                                    code = currentTrackHead + line,
                                    sinsyProxy = proxy
                                )

                                when {
                                    result.isRenderingNotation -> {
                                        errorPrintln("[mider-error] yet supported")
                                    }

                                    result.isSing -> stream.first().use {
                                        playWav(it.readAllBytes())
                                    }

                                    else -> stream.first().use {
                                        seq.setSequence(it)
                                        seq.open()
                                        seq.start()
                                    }
                                }
                            } catch (e: Throwable) {
                                errorPrintln("[mider-error] $e")
                            }
                        }
                    }
                }
            }
        }

    } else exitProcess(CommandLine(MiderCodeCommandLine()).execute(*args))
}