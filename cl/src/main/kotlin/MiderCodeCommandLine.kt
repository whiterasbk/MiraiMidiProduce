package org.mider.produce.cl

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.mider.produce.core.Configuration
import org.mider.produce.core.utils.generateAudioStreamByFormatMode
import org.mider.produce.core.utils.generateAudioStreamByFormatModeFromWav
import picocli.CommandLine.*
import whiter.music.mider.code.NotationType
import java.io.File
import java.util.concurrent.Callable
import javax.sound.midi.MidiSystem


@Command(name = "mccl", mixinStandardHelpOptions = true, version = ["0.1.9"],
    description = ["midercode command line tools"])
class MiderCodeCommandLine : Callable<Int> {

    @Parameters(index = "0", description = ["input midercode file, if only this argument provide, the the program will automatically play the midercode"])
    lateinit var file: File

    @Option(names = ["-proxy"], description = ["for internal ktor client"])
    var proxy: String? = null

    @Option(names = ["-o", "-output"], description = ["output file, can be .mid .mp3"])
    var output: String? = null

    @Option(names = ["-sinsyAlpha"], description = ["quality of generated sinsy wave, from -0.8 to 0.8"])
    var sinsySynAlpha: Float? = null

    @Option(names = ["-sinsyF0shift"], description = ["vibrato intensity of generated sinsy wave, from 0.0 to 2.0"])
    var sinsyF0shift: Int? = null

    @Option(names = ["-sinsyVibpower"], description = ["Pitch variable speed of generated sinsy wave, from -24 to 24"])
    var sinsyVibpower: Int? = null

    @Option(names = ["-sinsyClientRequestTimeoutMillis"], description = ["sinsy client request timeout"])
    var sinsyClientRequestTimeoutMillis: Long? = null

    @Option(names = ["-sinsyClientConnectTimeoutMillis"], description = ["sinsy client connect timeout"])
    var sinsyClientConnectTimeoutMillis: Long? = null

    @Option(names = ["-sinsyClientSocketTimeoutMillis"], description = ["sinsy client socket timeout"])
    var sinsyClientSocketTimeoutMillis: Long? = null

    @Option(names = ["-sinsyLink"], description = ["sinsy site link"])
    var sinsyLink: String? = null

    @Option(names = ["-ffmpegConvertCommand"], description = ["ffmpeg convert command"])
    var ffmpegConvertCommand: String? = null

    @Option(names = ["-timidityConvertCommand"], description = ["timidity convert command"])
    var timidityConvertCommand: String? = null

    @Option(names = ["-mscoreConvertMidi2Mp3Command"], description = ["mscore convert Midi to Mp3 command"])
    var mscoreConvertMidi2Mp3Command: String? = null

    @Option(names = ["-mscoreConvertMidi2MSCZCommand"], description = ["mscore convert Midi to MSCZ command"])
    var mscoreConvertMidi2MSCZCommand: String? = null

    @Option(names = ["-mscoreConvertMSCZ2PDFCommand"], description = ["mscore convert MSCZ to PDF command"])
    var mscoreConvertMSCZ2PDFCommand: String? = null

    @Option(names = ["-mscoreConvertMSCZ2PNGSCommand"], description = ["mscore convert MSCZ to PNGS command"])
    var mscoreConvertMSCZ2PNGSCommand: String? = null

    @Option(names = ["-recursionLimit"], description = ["macro recursion limit"])
    var recursionLimit: Int? = null

    @Option(names = ["-silkBitsRate"], description = ["silk bits rate"])
    var silkBitsRate: Int? = null

    @Option(names = ["-fm", "-formatMode"], description = ["format mode, for converting phase"])
    var formatMode: String? = null

    @Option(names = ["-macroUseStrictMode"], description = ["turn on macro strict mode"])
    var macroUseStrictMode: Boolean? = null

    @Option(names = ["-debug"], description = ["debug mode"])
    var debug: Boolean? = null

    @Option(names = ["-isBlankReplaceWith0"], description = ["replace blank with rest note in numeric notation"])
    var isBlankReplaceWith0: Boolean? = null

    @Option(names = ["-quality"], description = ["quality of generated lame format"])
    var quality: Int? = null

    override fun call(): Int = runBlocking {
        val (cfg, tmp) = getConfiguration()

        setupConfig(cfg)

        val (result, stream) = cfg.generate(
            code = file.readText(),
            sinsyCallback = { l1, l2 ->
                println("[mider-debug] fetching ... %$l1, %$l2")
            },
            sinsyProxy = proxy
        )

        output?.let {

            val file = File(it)

            if (!file.absoluteFile.parentFile.exists()) error("parent file of $file is not exist.")

            when {
                it.endsWith(".wav") -> when {
                    result.isSing -> file.writeBytes(stream.first().readAllBytes())
                    // result.isRenderingNotation -> error("visible format isn't able to convert into .wav")
                    // result.isUploadMidi -> error("midi format isn't able to convert into .wav")
                    else -> TODO("not support yet")
                }

                it.endsWith(".mp3") -> when {
                    result.isSing -> file.writeBytes(cfg
                        .generateAudioStreamByFormatModeFromWav(stream.first()).readAllBytes())

                     // result.isRenderingNotation -> error("visible format isn't able to convert into .mp3")
                     // result.isUploadMidi -> error("midi format isn't able to convert into .mp3")

                    else -> file.writeBytes(cfg.generateAudioStreamByFormatMode(stream.first()).readAllBytes())
                }

                it.endsWith(".mid") -> when {
                    // result.isSing -> error("wave format isn't able to convert into .mid")
                    // result.isRenderingNotation -> error("visible format isn't able to convert into .mid")
                    else -> file.writeBytes(stream.first().readAllBytes())
                }

                it.endsWith(".pdf") -> when {
                    result.isRenderingNotation && result.notationType == NotationType.PDF -> file.writeBytes(stream.first().readAllBytes())
                    else -> error("current stream is not stander pdf stream")
                }

                it.endsWith(".mscz") -> when {
                    result.isRenderingNotation && result.notationType == NotationType.MSCZ -> file.writeBytes(stream.first().readAllBytes())
                    else -> error("current stream is not stander mscz stream")
                }

                it.endsWith(".png") -> when {
                    result.isRenderingNotation && result.notationType == NotationType.PNGS -> {
                        if (stream.size == 1) {
                            file.writeBytes(stream.first().readAllBytes())
                        } else {
                            for ((count, ss) in stream.withIndex()) {
                                File(file.absoluteFile.parentFile, file.nameWithoutExtension + "-" + (count + 1) + ".png")
                                    .writeBytes(ss.readAllBytes())
                            }
                        }
                    }
                    else -> error("current stream is not stander png stream")
                }

                else -> error("unsupported format")
            }
        } ?: when {
            result.isRenderingNotation -> {
                TODO("not yet supported")
            }

            result.isSing -> stream.first().use {
                playWav(it.readAllBytes())
            }

            else -> stream.first().use {
                val sequencer = MidiSystem.getSequencer()
                sequencer.setSequence(it)
                sequencer.open()
                sequencer.start()
                delay(sequencer.sequence.microsecondLength / 1000 + 500)
            }
        }

        tmp.deleteOnExit()

        0
    }

    private fun setupConfig(cfg: Configuration) {
        sinsySynAlpha?.let { cfg.sinsySynAlpha = it }
        sinsyF0shift?.let { cfg.sinsyF0shift = it }
        sinsyVibpower?.let { cfg.sinsyVibpower = it }
        sinsyLink?.let { cfg.sinsyLink = it }
        sinsyClientConnectTimeoutMillis?.let { cfg.sinsyClientConnectTimeoutMillis = it }
        sinsyClientRequestTimeoutMillis?.let { cfg.sinsyClientRequestTimeoutMillis = it }
        sinsyClientSocketTimeoutMillis?.let { cfg.sinsyClientSocketTimeoutMillis = it }
        ffmpegConvertCommand?.let { cfg.ffmpegConvertCommand = it }
        timidityConvertCommand?.let { cfg.timidityConvertCommand = it }
        mscoreConvertMSCZ2PDFCommand?.let { cfg.mscoreConvertMSCZ2PDFCommand = it }
        mscoreConvertMSCZ2PNGSCommand?.let { cfg.mscoreConvertMSCZ2PNGSCommand = it }
        mscoreConvertMidi2MSCZCommand?.let { cfg.mscoreConvertMidi2MSCZCommand = it }
        mscoreConvertMidi2Mp3Command?.let { cfg.mscoreConvertMidi2Mp3Command = it }
        recursionLimit?.let { cfg.recursionLimit = it }
        silkBitsRate?.let { cfg.silkBitsRate = it }
        formatMode?.let { cfg.formatMode = it }
        macroUseStrictMode?.let { cfg.macroUseStrictMode = it }
        debug?.let { cfg.debug = it }
        isBlankReplaceWith0?.let { cfg.isBlankReplaceWith0 = it }
        quality?.let { cfg.quality = it }
    }
}
