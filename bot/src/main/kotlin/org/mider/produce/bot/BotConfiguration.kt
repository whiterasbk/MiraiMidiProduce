package org.mider.produce.bot

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import org.mider.produce.core.Configuration


object BotConfiguration : AutoSavePluginConfig("config") {
    @ValueDescription("sinsy 生成语音的声质, -0.8~0.8")
    val sinsySynAlpha by value(0.55f)
    @ValueDescription("sinsy 颤音强度, 0.0~2.0")
    val sinsyF0shift by value(0)
    @ValueDescription("sinsy 俯仰变速, -24~24")
    val sinsyVibpower by value(1)
    @ValueDescription("sinsy 接口")
    val sinsyLink by value("http://sinsy.sp.nitech.ac.jp")

    @ValueDescription("sinsyClientRequestTimeoutMillis")
    val sinsyClientRequestTimeoutMillis: Long by value(5 * 60_000L)
    @ValueDescription("sinsyClientConnectTimeoutMillis")
    val sinsyClientConnectTimeoutMillis: Long by value(5 * 60_000L)
    @ValueDescription("sinsyClientSocketTimeoutMillis")
    val sinsyClientSocketTimeoutMillis: Long by value(5 * 60_000L)

    @ValueDescription("上传文件的触发格式")
    val miderCodeFormatName by value("midercode")
    @ValueDescription("2000year")
    val selfMockeryTime by value(7 * 1000L)
    @ValueDescription("is2000year(")
    val selfMockery by value(false)

    @ValueDescription("命令执行超时时间")
    val commandTimeout by value(60 * 1000L)

    @ValueDescription("ffmpeg 转换命令 (不使用 ffmpeg 也可以, 只要能完成 wav 到 mp3 的转换就行, {{input}} 和 {{output}} 由 插件提供不需要修改")
    val ffmpegConvertCommand by value("ffmpeg -i {{input}} -acodec libmp3lame -ab 256k {{output}}")
    @ValueDescription("timidity 转换命令 (不使用 timidity 也可以, 只要能完成 mid 到 wav 的转换就行")
    val timidityConvertCommand by value("timidity {{input}} -Ow -o {{output}}")
    @ValueDescription("muse score 从 .mid 转换到 .mp3 ")
    val mscoreConvertMidi2Mp3Command by value("MuseScore3 {{input}} -o {{output}}")

    @ValueDescription("muse score 从 .mid 转换到 .mscz")
    val mscoreConvertMidi2MSCZCommand by value("MuseScore3 {{input}} -o {{output}}")

    @ValueDescription("muse score 从 .mid 转换到 .pdf")
    val mscoreConvertMSCZ2PDFCommand by value("MuseScore3 {{input}} -o {{output}}")

    @ValueDescription("muse score 从 .mid 转换到 .png 序列")
    val mscoreConvertMSCZ2PNGSCommand by value("MuseScore3 {{input}} -o {{output}} --trim-image 120")

    @ValueDescription("include 最大深度")
    val recursionLimit by value(50)
    @ValueDescription("silk 比特率(吧")
    val silkBitsRate by value(24000)
    @ValueDescription("是否启用缓存")
    val cache by value(true)

    @ValueDescription("生成模式, 可选的有: \n" +
            "internal->java-lame (默认)\n" +
            "internal->java-lame->silk4j\n" +
            "timidity->ffmpeg\n" +
            "timidity->ffmpeg->silk4j\n" +
            "timidity->java-lame\n" +
            "timidity->java-lame->silk4j\n" +
            "muse-score\n" +
            "muse-score->silk4j\n"
    )
    var formatMode by value("internal->java-lame")
    @ValueDescription("宏是否启用严格模式")
    val macroUseStrictMode by value(true)
    @ValueDescription("是否启用调试")
    val debug by value(false)
    @ValueDescription("是否启用空格替换")
    val isBlankReplaceWith0 by value(true)
    @ValueDescription("量化深度 理论上越大生成 mp3 的质量越好, java-lame 给出的值是 256")
    val quality by value(64)
    @ValueDescription("超过这个大小则自动改为文件上传")
    val uploadSize by value(1153433L)
    @ValueDescription("帮助信息 (更新版本时记得要删掉这一行)")
    val help by value("https://github.com/whiterasbk/MiraiMidiProduce/blob/master/README.md")

    fun copy(coreCfg: Configuration) {
        coreCfg.sinsySynAlpha = sinsySynAlpha
        coreCfg.sinsyF0shift = sinsyF0shift
        coreCfg.sinsyVibpower = sinsyVibpower
        coreCfg.sinsyLink = sinsyLink

        coreCfg.sinsyClientRequestTimeoutMillis = sinsyClientRequestTimeoutMillis
        coreCfg.sinsyClientConnectTimeoutMillis = sinsyClientConnectTimeoutMillis
        coreCfg.sinsyClientSocketTimeoutMillis = sinsyClientSocketTimeoutMillis

        coreCfg.miderCodeFormatName = miderCodeFormatName
        coreCfg.selfMockeryTime = selfMockeryTime
        coreCfg.selfMockery = selfMockery
        coreCfg.commandTimeout = commandTimeout
        coreCfg.ffmpegConvertCommand = ffmpegConvertCommand
        coreCfg.timidityConvertCommand = timidityConvertCommand
        coreCfg.mscoreConvertMidi2Mp3Command = mscoreConvertMidi2Mp3Command
        coreCfg.mscoreConvertMidi2MSCZCommand = mscoreConvertMidi2MSCZCommand
        coreCfg.mscoreConvertMSCZ2PDFCommand = mscoreConvertMSCZ2PDFCommand
        coreCfg.mscoreConvertMSCZ2PNGSCommand = mscoreConvertMSCZ2PNGSCommand
        coreCfg.recursionLimit = recursionLimit
        coreCfg.silkBitsRate = silkBitsRate
        coreCfg.cache = cache
        coreCfg.formatMode = formatMode
        coreCfg.macroUseStrictMode = macroUseStrictMode
        coreCfg.debug = debug
        coreCfg.isBlankReplaceWith0 = isBlankReplaceWith0
        coreCfg.quality = quality
        coreCfg.uploadSize = uploadSize
        coreCfg.help = help
    }
}