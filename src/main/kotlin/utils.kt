package bot.music.whiter

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.content
import net.sourceforge.lame.lowlevel.LameEncoder
import net.sourceforge.lame.mp3.Lame
import net.sourceforge.lame.mp3.MPEGMode
import whiter.music.mider.dsl.MiderDSL
import whiter.music.mider.dsl.fromDsl
import java.io.*
import java.util.concurrent.TimeUnit
import javax.sound.sampled.AudioSystem

suspend fun MessageEvent.matchRegex(reg: Regex, block: suspend (String) -> Unit) {
    if (message.content.matches(reg)) {
        block(message.content)
    }
}

suspend fun MessageEvent.matchRegex(reg: String, block: suspend (String) -> Unit) = matchRegex(Regex(reg), block)

fun String.runCommand(workingDir: File, prefix: String = "cmd-", inPipe: Boolean = false): String? {
    try {
        val parts = this.split("\\s".toRegex())
        val proc = (if (inPipe) {
            ProcessBuilder(*parts.toTypedArray())
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
        } else {
            val output = File(workingDir, "${prefix}out.txt")
            val error = File(workingDir,"${prefix}err.txt")
            ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(output)
            .redirectError(error)
        }).start()

        proc.waitFor(60, TimeUnit.MINUTES)
        return proc.inputStream.bufferedReader().readText()
    } catch(e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun midi2mp3Stream(USE_VARIABLE_BITRATE: Boolean = false, GOOD_QUALITY_BITRATE: Int = 256, block: MiderDSL.() -> Any): ByteArrayInputStream {

    val midiFile = fromDsl(block)
    val audioInputStream = AudioSystem.getAudioInputStream(midiFile.inStream())

    val encoder = LameEncoder(
        audioInputStream.format,
        GOOD_QUALITY_BITRATE,
        MPEGMode.STEREO,
        Lame.QUALITY_HIGHEST,
        USE_VARIABLE_BITRATE
    )

    val mp3 = ByteArrayOutputStream()
    val inputBuffer = ByteArray(encoder.pcmBufferSize)
    val outputBuffer = ByteArray(encoder.pcmBufferSize)
    var bytesRead: Int
    var bytesWritten: Int
    while (0 < audioInputStream.read(inputBuffer).also { bytesRead = it }) {
        bytesWritten = encoder.encodeBuffer(inputBuffer, 0, bytesRead, outputBuffer)
        mp3.write(outputBuffer, 0, bytesWritten)
    }

    encoder.close()
    return ByteArrayInputStream(mp3.toByteArray())
}

fun ifDebug(block: ()-> Unit) {
    if (Config.debug) block()
}

fun noteBaseOffset(note: String): Int {
    return when (note) {
        "C", "#B" -> 0
        "#C", "bD" -> 1
        "D" -> 2
        "#D", "bE" -> 3
        "E", "bF" -> 4
        "F", "#E" -> 5
        "#F", "bG" -> 6
        "G" -> 7
        "#G", "bA" -> 8
        "A" -> 9
        "#A", "bB" -> 10
        "B", "bC" -> 11
        else -> throw Exception("no such note $note")
    }
}

fun noteNameFromCode(code: Int): String {
    return when(code % 12) {
        0 -> "C"
        1 -> "#C"
        2 -> "D"
        3 -> "#D"
        4 -> "E"
        5 -> "F"
        6 -> "#F"
        7 -> "G"
        8 -> "#G"
        9 -> "A"
        10 -> "#A"
        11 -> "B"
        else -> throw Exception("no such note code: $code")
    }
}

fun charCount(str: CharSequence, char: Char): Int {
    return str.filter { it == char }.count()
}

fun deriveInterval(index: Int, scale: Array<Int> = arrayOf(2, 2, 1, 2, 2, 2, 1)): Int {
    var sum = 0
    for (i in 0 until index) {
        sum += scale[i]
    }
    return sum
}

fun nextNoteIntervalInMajorScale(code: Int): Int {
    return when(code % 12) {
        0 -> 2  // C
        1 -> 2  // C#
        2 -> 2  // D
        3 -> 2  // D#
        4 -> 1  // E
        5 -> 2  // F
        6 -> 2  // F#
        7 -> 2  // G
        8 -> 2  // G#
        9 -> 2  // A
        10 -> 2 // A#
        11 -> 1 // B
        else -> 2
    }
}

fun previousNoteIntervalInMajorScale(code: Int): Int {
    return when(code % 12) {
        0 -> 1  // C
        1 -> 2  // C#
        2 -> 2  // D
        3 -> 2  // D#
        4 -> 2  // E
        5 -> 1  // F
        6 -> 2  // F#
        7 -> 2  // G
        8 -> 2  // G#
        9 -> 2  // A
        10 -> 2 // A#
        11 -> 2 // B
        else -> 2
    }
}



