package org.mider.produce.core.utils

import io.github.mzdluo123.silk4j.AudioUtils
import net.sourceforge.lame.lowlevel.LameEncoder
import net.sourceforge.lame.mp3.Lame
import net.sourceforge.lame.mp3.MPEGMode
import org.mider.produce.core.*
import whiter.music.mider.dsl.MiderDSL
import whiter.music.mider.dsl.fromDsl
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem


fun Configuration.generateAudioStreamByFormatMode(block: MiderDSL.() -> Unit): InputStream
    = generateAudioStreamByFormatMode(fromDsl(block).inStream())

fun midi2mp3Stream(USE_VARIABLE_BITRATE: Boolean = false, GOOD_QUALITY_BITRATE: Int = 256, midiStream: InputStream): ByteArrayInputStream {
    val audioInputStream = AudioSystem.getAudioInputStream(midiStream)
    return wave2mp3Stream(audioInputStream, USE_VARIABLE_BITRATE, GOOD_QUALITY_BITRATE)
}

fun wave2mp3Stream(audioInputStream: AudioInputStream, USE_VARIABLE_BITRATE: Boolean = false, GOOD_QUALITY_BITRATE: Int = 256): ByteArrayInputStream {
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

fun Configuration.generateAudioStreamByFormatModeFromWav(wavStream: InputStream): InputStream {
    return when (formatMode) {
        "internal->java-lame->silk4j" -> {
            val mp3 = wave2mp3Stream(AudioSystem.getAudioInputStream(wavStream), GOOD_QUALITY_BITRATE = quality)
            val silk = AudioUtils.mp3ToSilk(mp3, silkBitsRate)
            silk.deleteOnExit()
            silk.inputStream()
        }

        "timidity->ffmpeg" -> {
            val mp3 = ffmpegConvert(wavStream)
            mp3.inputStream()
        }

        "timidity->ffmpeg->silk4j" -> {
            val mp3 = ffmpegConvert(wavStream)
            val silk = AudioUtils.mp3ToSilk(mp3, silkBitsRate)
            silk.deleteOnExit()
            silk.inputStream()
        }

        else -> {
            // internal->java-lame
            wave2mp3Stream(AudioSystem.getAudioInputStream(wavStream), GOOD_QUALITY_BITRATE = quality)
        }
    }
}

fun Configuration.generateAudioStreamByFormatMode(midiStream: InputStream): InputStream {
    return when (formatMode) {
        "internal->java-lame->silk4j" -> {
            ifDebug("using: internal->java-lame->silk4j")
            val audioInputStream = midi2mp3Stream(GOOD_QUALITY_BITRATE = quality, midiStream = midiStream)
            val silk = AudioUtils.mp3ToSilk(audioInputStream, silkBitsRate)
            silk.deleteOnExit()
            silk.inputStream()
        }

        "internal->silk4j" -> {
            // todo fix sampleRate 和 bitRate 怎么也对不上的问题
            ifDebug("using: internal->silk4j")
            val pcmStream = AudioSystem.getAudioInputStream(midiStream)
            val sampleRate = pcmStream.format.sampleRate.toInt()
            val bitRate = pcmStream.format.sampleSizeInBits
            val pcmFile = audioUtilsGetTempFile("pcm").let { it.writeBytes(pcmStream.readAllBytes()); it }
            audioUtilsPcmToSilk(pcmFile, sampleRate, bitRate).inputStream()
        }

        "timidity->ffmpeg" -> {
            ifDebug("using: timidity->ffmpeg")
            val wav = timidityConvert(midiStream)
            val mp3 = ffmpegConvert(wav.inputStream())
            mp3.inputStream()
        }

        "timidity->ffmpeg->silk4j" -> {
            ifDebug("timidity->ffmpeg->silk4j")
            val wav = timidityConvert(midiStream)
            val mp3 = ffmpegConvert(wav.inputStream())
            val silk = AudioUtils.mp3ToSilk(mp3, silkBitsRate)
            silk.deleteOnExit()
            silk.inputStream()
        }

        "timidity->silk4j" -> {
            ifDebug("using: timidity->silk4j")
            TODO("not yet implement: 暂未支持操作")
        }

        "timidity->java-lame" -> {
            ifDebug("using: timidity->java-lame")
            val wav = timidityConvert(midiStream)
            wave2mp3Stream(AudioSystem.getAudioInputStream(wav), GOOD_QUALITY_BITRATE = quality)
        }

        "timidity->java-lame->silk4j" -> {
            ifDebug("using: timidity->java-lame->silk4j")
            val wav = timidityConvert(midiStream)
            val mp3Stream = wave2mp3Stream(AudioSystem.getAudioInputStream(wav), GOOD_QUALITY_BITRATE = quality)
            val silk = AudioUtils.mp3ToSilk(mp3Stream, silkBitsRate)
            silk.deleteOnExit()
            silk.inputStream()
        }

        "muse-score" -> {
            ifDebug("using: muse-score")
            // support instrument
            museScoreConvert(midiStream).inputStream()
        }

        "muse-score->silk4j" -> {
            ifDebug("using: muse-score->silk4j")
            // support instrument
            val silk = AudioUtils.mp3ToSilk(museScoreConvert(midiStream, ).inputStream(), silkBitsRate)
            silk.deleteOnExit()
            silk.inputStream()
        }

        // internal->java-lame 或者默认情况
        else -> {
            ifDebug("using: internal->java-lame")
            midi2mp3Stream(GOOD_QUALITY_BITRATE = quality, midiStream = midiStream)
        }
    }
}