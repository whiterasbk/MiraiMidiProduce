package bot.music.whiter

import io.github.mzdluo123.silk4j.AudioUtils
import io.github.mzdluo123.silk4j.SilkCoder
import net.mamoe.mirai.contact.AudioSupported
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.sourceforge.lame.lowlevel.LameEncoder
import net.sourceforge.lame.mp3.Lame
import net.sourceforge.lame.mp3.MPEGMode
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.ExecuteWatchdog
import org.apache.commons.exec.PumpStreamHandler
import whiter.music.mider.code.produceCore
import whiter.music.mider.dsl.MiderDSL
import whiter.music.mider.dsl.fromDsl
import whiter.music.mider.dsl.fromDslInstance
import whiter.music.mider.dsl.playDslInstance
import java.io.*
import java.nio.charset.Charset
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.math.pow


suspend fun String.matchRegex(reg: Regex, block: suspend (String) -> Unit) {
    if (this.matches(reg)) {
        block(this)
    }
}

suspend fun MessageEvent.matchRegex(reg: Regex, block: suspend (String) -> Unit) = message.content.matchRegex(reg, block)

suspend fun MessageEvent.matchRegex(reg: String, block: suspend (String) -> Unit) = matchRegex(Regex(reg), block)

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

fun ifDebug(block: ()-> Unit) {
    if (Config.debug) block()
}

fun ifDebug(info: String) {
    if (Config.debug) MidiProduce.logger.info(info)
}

fun Long.autoTimeUnit(): String {
    return if (this < 1000) {
        "${this}ms"
    } else if (this in 1000..59999) {
        "${ String.format("%.2f", this.toFloat() / 1000) }s"
    } else {
        "${ this / 60_000 }m${ (this % 60_000) / 1000 }s"
    }
}

suspend fun time(block: suspend () -> Unit) {
    if (Config.debug) {
        val startCountingTime = System.currentTimeMillis()
        block()
        val useTime = System.currentTimeMillis() - startCountingTime
        MidiProduce.logger.info("生成用时: ${useTime.autoTimeUnit()}")
    } else block()
}

fun generateAudioStreamByFormatModeFromWav(wavStream: InputStream): InputStream {
    return when (Config.formatMode) {
        "internal->java-lame->silk4j" -> {
            val mp3 = wave2mp3Stream(AudioSystem.getAudioInputStream(wavStream), GOOD_QUALITY_BITRATE = Config.quality)
            val silk = AudioUtils.mp3ToSilk(mp3, Config.silkBitsRate)
            silk.deleteOnExit()
            silk.inputStream()
        }

        "timidity->ffmpeg" -> {
            val mp3 = ffmpegConvert(wavStream)
            mp3.inputStream()
        }

        "timidity->ffmpeg->silk4j" -> {
            val mp3 = ffmpegConvert(wavStream)
            val silk = AudioUtils.mp3ToSilk(mp3, Config.silkBitsRate)
            silk.deleteOnExit()
            silk.inputStream()
        }

        else -> {
            // internal->java-lame
            wave2mp3Stream(AudioSystem.getAudioInputStream(wavStream), GOOD_QUALITY_BITRATE = Config.quality)
        }
    }
}

fun generateAudioStreamByFormatMode(midiStream: InputStream): InputStream {
    return when (Config.formatMode) {
        "internal->java-lame->silk4j" -> {
            ifDebug("using: internal->java-lame->silk4j")
            val audioInputStream = midi2mp3Stream(GOOD_QUALITY_BITRATE = Config.quality, midiStream = midiStream)
            val silk = AudioUtils.mp3ToSilk(audioInputStream, Config.silkBitsRate)
            silk.deleteOnExit()
            silk.inputStream()
        }

        "internal->silk4j" -> {
            // todo fix sampleRate 和 bitRate 怎么也对不上的问题
            ifDebug("using: internal->silk4j")
            val pcmStream = AudioSystem.getAudioInputStream(midiStream)
            val sampleRate = pcmStream.format.sampleRate.toInt()
            val bitRate = pcmStream.format.sampleSizeInBits
            val pcmFile = AudioUtilsGetTempFile("pcm").let { it.writeBytes(pcmStream.readAllBytes()); it }
            AudioUtilsPcmToSilk(pcmFile, sampleRate, bitRate).inputStream()
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
            val silk = AudioUtils.mp3ToSilk(mp3, Config.silkBitsRate)
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
            wave2mp3Stream(AudioSystem.getAudioInputStream(wav), GOOD_QUALITY_BITRATE = Config.quality)
        }

        "timidity->java-lame->silk4j" -> {
            ifDebug("using: timidity->java-lame->silk4j")
            val wav = timidityConvert(midiStream)
            val mp3Stream = wave2mp3Stream(AudioSystem.getAudioInputStream(wav), GOOD_QUALITY_BITRATE = Config.quality)
            val silk = AudioUtils.mp3ToSilk(mp3Stream, Config.silkBitsRate)
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
            val silk = AudioUtils.mp3ToSilk(museScoreConvert(midiStream).inputStream(), Config.silkBitsRate)
            silk.deleteOnExit()
            silk.inputStream()
        }

        // internal->java-lame 或者默认情况
        else -> {
            ifDebug("using: internal->java-lame")
            midi2mp3Stream(GOOD_QUALITY_BITRATE = Config.quality, midiStream = midiStream)
        }
    }
}

fun generateAudioStreamByFormatMode(block: MiderDSL.() -> Unit): InputStream = generateAudioStreamByFormatMode(fromDsl(block).inStream())

fun timidityConvert(midiFileStream: InputStream): File {
    val midiFile = AudioUtilsGetTempFile("mid")
    midiFile.writeBytes(midiFileStream.readAllBytes())
    val outputFile = AudioUtilsGetTempFile("wav")

    val result = Config.timidityConvertCommand
        .replace("{{input}}", midiFile.name)
        .replace("{{output}}", outputFile.name)
        .execute()

    if (!outputFile.exists() || outputFile.length() == 0L) throw Exception(result.second)
    ifDebug(result.first)

    return outputFile
}

fun ffmpegConvert(midiFileStream: InputStream): File {
    val wavFile = AudioUtilsGetTempFile("wav")
    wavFile.writeBytes(midiFileStream.readAllBytes())
    val outputFile = AudioUtilsGetTempFile("mp3")

    val result = Config.ffmpegConvertCommand
        .replace("{{input}}", wavFile.name)
        .replace("{{output}}", outputFile.name)
        .execute()

    if (!outputFile.exists() || outputFile.length() == 0L) throw Exception(result.second)
    ifDebug(result.first)

    return outputFile
}

fun museScoreConvert(midiFileStream: InputStream): File {
    val midiFile = AudioUtilsGetTempFile("mid")
    midiFile.writeBytes(midiFileStream.readAllBytes())
    return convertUsingConfigCommand(Config.mscoreConvertMidi2Mp3Command, midiFile, "mp3")
}

fun convert2MSCZ(midi: File): File {
   return convertUsingConfigCommand(Config.mscoreConvertMidi2MSCZCommand, midi, "mscz")
}

fun convert2PDF(midi: File): File {
   return convertUsingConfigCommand(Config.mscoreConvertMSCZ2PDFCommand, midi, "pdf")
}

fun convert2PNGS(midi: File): List<File> {
    val outputSample = AudioUtilsGetTempFile("png")
    outputSample.writeText("大弦嘈嘈如急雨, 小弦切切如私语")
    convertUsingConfigCommand(Config.mscoreConvertMSCZ2PNGSCommand, midi, outputSample)

    val result = outputSample.parentFile.listFiles(FileFilter {
        it.name.startsWith(outputSample.nameWithoutExtension) && it != outputSample
    }) ?: throw Exception("convert to pngs failed")

    return result.toList().sorted()
}

private fun convertUsingConfigCommand(usingCommand: String, inputFile: File, outputFile: File): File {
    val result = usingCommand
        .replace("{{input}}", inputFile.name)
        .replace("{{output}}", outputFile.name)
        .execute()

    if (!outputFile.exists() || outputFile.length() == 0L) throw Exception(result.second)

    ifDebug(result.first)

    return outputFile
}

private fun convertUsingConfigCommand(usingCommand: String, inputFile: File, outputFileExtension: String): File {
    return convertUsingConfigCommand(usingCommand, inputFile, AudioUtilsGetTempFile(outputFileExtension))
}

@Throws(IOException::class)
private fun AudioUtilsPcmToSilk(pcmFile: File, sampleRate: Int, bitRate: Int = 24000): File {
    if (!pcmFile.exists() || pcmFile.length() == 0L) {
        throw IOException("文件不存在或为空")
    }
    val silkFile = AudioUtilsGetTempFile("silk")
    SilkCoder.encode(pcmFile.absolutePath, silkFile.absolutePath, sampleRate, bitRate)
    return silkFile
}

fun AudioUtilsGetTempFile(type: String, autoClean: Boolean = true): File {
    val fileName = "mirai_audio_${type}_${System.currentTimeMillis()}.$type"
    return File(MidiProduce.tmpDir, fileName).let { if (autoClean) it.deleteOnExit(); it }
}

fun String.execute(charset: String, timeout: Long = Config.commandTimeout, workingDir: File = MidiProduce.tmpDir) = this.execute(Charset.forName(charset), timeout , workingDir)

fun String.execute(charset: Charset = Charset.forName("utf-8"), timeout: Long = Config.commandTimeout, workingDir: File = MidiProduce.tmpDir): Pair<String, String> {
    //接收正常结果流
    val outputStream = ByteArrayOutputStream()
    //接收异常结果流
    val errorStream = ByteArrayOutputStream()
    val commandline: CommandLine = CommandLine.parse(this)
    val exec = DefaultExecutor()
    exec.workingDirectory = workingDir
    exec.setExitValues(null)
    val watchdog = ExecuteWatchdog(timeout)
    exec.watchdog = watchdog
    val streamHandler = PumpStreamHandler(outputStream, errorStream)
    exec.streamHandler = streamHandler
    exec.execute(commandline)
    //不同操作系统注意编码，否则结果乱码
    val out = outputStream.toString(charset)
    val error = errorStream.toString(charset)
    return out to error
}

fun String.toPinyin(): String {
    val pyf = HanyuPinyinOutputFormat()
    // 设置大小写
    pyf.caseType = HanyuPinyinCaseType.LOWERCASE
    // 设置声调表示方法
    pyf.toneType = HanyuPinyinToneType.WITH_TONE_NUMBER
    // 设置字母u表示方法
    pyf.vCharType = HanyuPinyinVCharType.WITH_V

    val sb = StringBuilder()
    val regex = Regex("[\\u4E00-\\u9FA5]+")

    for (i in indices) {
        // 判断是否为汉字字符
        if (regex.matches(this[i].toString())) {
            val s = PinyinHelper.toHanyuPinyinStringArray(this[i], pyf)
            if (s != null)
                sb.append(s[0])
        } else sb.append(this[i])
    }

    return sb.toString()
}

fun playMiderCodeFile(path: String) {
    val r = produceCore(File(path).readText())
    playDslInstance(miderDSL = r.miderDSL)
}

suspend fun MessageEvent.sendAudioMessage(origin: String, stream: InputStream, attachUploadFileName: String? = null) {
    when (this) {
        is GroupMessageEvent -> {
            val size = stream.available()
            if (size > 1024 * 1024) MidiProduce.logger.info("文件大于 1m 可能导致语音无法播放, 大于 upload size 时将自动转为文件上传")
            if (size > Config.uploadSize) {
                stream.toExternalResource().use {
                    group.files.uploadNewFile(
                         if (attachUploadFileName != null) "$attachUploadFileName-" else "" +
                                 "generate-${System.currentTimeMillis()}.mp3", it
                    )
                }
            } else {
                stream.toExternalResource().use {
                    val audio = group.uploadAudio(it)
                    group.sendMessage(audio)
                    if (Config.cache) MidiProduce.cache[origin] = audio
                }
            }
        }

        is FriendMessageEvent -> {
            if (stream.available() > Config.uploadSize) {
                friend.sendMessage("生成的语音过大且bot不能给好友发文件")
            } else {
                stream.toExternalResource().use {
                    val audio = friend.uploadAudio(it)
                    friend.sendMessage(audio)
                    if (Config.cache) MidiProduce.cache[origin] = audio
                }
            }
        }

        else -> throw Exception("打咩")
    }
}

suspend fun AudioSupported.sendMiderCode(code: String) {
    val result = produceCore(code, MidiProduce.produceCoreConfiguration)
    val midiStream = fromDslInstance(result.miderDSL).inStream()
    val audioStream = generateAudioStreamByFormatMode(midiStream)
    audioStream.toExternalResource().use {
        val audio = uploadAudio(it)
        sendMessage(audio)
    }
}