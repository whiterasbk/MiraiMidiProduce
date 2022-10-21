package org.mider.produce.core.utils

import io.github.mzdluo123.silk4j.SilkCoder
import org.mider.produce.core.Configuration
import whiter.music.mider.code.produceCore
import whiter.music.mider.dsl.playDslInstance
import java.io.File
import java.io.IOException

@Throws(IOException::class)
internal fun Configuration.audioUtilsPcmToSilk(pcmFile: File, sampleRate: Int, bitRate: Int = 24000): File {
    if (!pcmFile.exists() || pcmFile.length() == 0L) {
        throw IOException("文件不存在或为空")
    }
    val silkFile = audioUtilsGetTempFile("silk", false)
    SilkCoder.encode(pcmFile.absolutePath, silkFile.absolutePath, sampleRate, bitRate)
    return silkFile
}

fun Configuration.audioUtilsGetTempFile(type: String, autoClean: Boolean = true): File {
    val fileName = "mirai_audio_${type}_${System.currentTimeMillis()}.$type"
    return File(tmpDir, fileName).let { if (autoClean) it.deleteOnExit(); it }
}

fun playMiderCodeFile(path: String) {
    val r = produceCore(File(path).readText())
    playDslInstance(miderDSL = r.miderDSL)
}