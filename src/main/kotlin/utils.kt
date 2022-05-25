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




