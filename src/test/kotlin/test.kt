package bot.music.whiter

import net.sourceforge.lame.lowlevel.LameEncoder
import whiter.music.mider.dsl.apply
import javax.sound.sampled.AudioFileFormat

import javax.sound.sampled.AudioSystem

import javax.sound.sampled.AudioInputStream

import net.sourceforge.lame.mp3.Lame

import net.sourceforge.lame.mp3.MPEGMode
import whiter.music.mider.MidiFile
import whiter.music.mider.dsl.MiderDSL
import whiter.music.mider.dsl.fromDsl
import java.io.*


fun main(args: Array<String>) {
    val s = midi2mp3Stream {
        !toMiderNoteList("c~g#+~a$~g|")
        debug()
    }

    // Convert("src/test/resources/1.mid", "src/test/resources/ui.wav")
    File("src/test/resources/aya.mp3").writeBytes(s.readAllBytes())
}

fun midi2mp3Stream(USE_VARIABLE_BITRATE: Boolean = false, GOOD_QUALITY_BITRATE: Int = 256, block: MiderDSL.() -> Any): InputStream {

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


fun Convert(midipath: String?, wavpath: String?) {
    val audioStream = AudioSystem.getAudioInputStream(File(midipath))
    AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, File(wavpath))
}