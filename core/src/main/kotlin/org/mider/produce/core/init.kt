package org.mider.produce.core

import io.github.mzdluo123.silk4j.AudioUtils
import java.io.FileFilter

fun Configuration.initTmpAndFormatTransfer() {
    if (!tmpDir.exists()) tmpDir.mkdir()

    // unimportant
    val tty = resolveFileAction("2000-years-later.png")
    if (!tty.exists())
        this.javaClass.classLoader.getResourceAsStream("2000-years-later.png")?.let {
            tty.writeBytes(it.readAllBytes())
        } ?: info("can not release 2000-years-later.png")

    try {
        tmpDir.listFiles(FileFilter {
            when(it.extension) {
                "so", "dll", "lib", "mp3", "silk", "wave", "wav", "amr",
                "mid", "midi", "mscz", "png", "pdf", "pcm", "xml"
                -> true
                else -> false
            }
        })?.forEach {
            it.delete()
        }
    } catch (e: Exception) {
        error("清理缓存失败")
        error(e)
    }

    if (formatMode.contains("silk4j")) {
        try {
            AudioUtils.init(tmpDir)
        } catch (e: Exception) {
            error("silk4j 加载失败, 将无法生成语音")
            error(e)
        }
    }

    if (formatMode.contains("timidity") && timidityConvertCommand.isBlank()) {
        error("timidity 命令未配置, 将无法生成语音(wav)")
    }

    if (formatMode.contains("ffmpeg") && ffmpegConvertCommand.isBlank()) {
        error("ffmpeg 命令未配置, 将无法生成语音(mp3)")
    }
}