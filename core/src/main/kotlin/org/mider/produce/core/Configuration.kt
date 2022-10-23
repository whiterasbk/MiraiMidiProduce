package org.mider.produce.core

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.io.File

/**
 * @param sinsySynAlpha sinsy 生成语音的声质, -0.8~0.8
 * @param sinsyF0shift sinsy 颤音强度, 0.0~2.0
 * @param sinsyVibpower sinsy 俯仰变速, -24~24
 * @param sinsyLink sinsy 接口
 * @param miderCodeFormatName 上传文件的触发格式
 * @param selfMockery is2000year
 * @param selfMockeryTime 2000year
 * @param commandTimeout 命令执行超时时间
 * @param ffmpegConvertCommand ffmpeg 转换命令 (不使用 ffmpeg 也可以, 只要能完成 wav 到 mp3 的转换就行, {{input}} 和 {{output}} 由 插件提供不需要修改
 * @param timidityConvertCommand timidity 转换命令 (不使用 timidity 也可以, 只要能完成 mid 到 wav 的转换就行
 * @param mscoreConvertMidi2Mp3Command score 从 .mid 转换到 .mp3
 * @param mscoreConvertMidi2MSCZCommand muse score 从 .mid 转换到 .mscz
 * @param mscoreConvertMSCZ2PDFCommand muse score 从 .mid 转换到 .pdf
 * @param mscoreConvertMSCZ2PNGSCommand muse score 从 .mid 转换到 .png 序列
 * @param recursionLimit include 最大深度
 * @param silkBitsRate silk 比特率(吧
 * @param cache 是否启用缓存
 * @param formatMode 生成模式, 可选的有: internal->java-lame (默认) internal->java-lame->silk4j timidity->ffmpeg timidity->ffmpeg->silk4j timidity->java-lame timidity->java-lame->silk4j muse-score muse-score->silk4j
 * @param macroUseStrictMode 宏是否启用严格模式
 * @param debug 是否启用调试
 * @param isBlankReplaceWith0 是否启用空格替换
 * @param quality 量化深度 理论上越大生成 mp3 的质量越好, java-lame 给出的值是 256
 * @param uploadSize 超过这个大小则自动改为文件上传
 * @param help 帮助信息 (更新版本时记得要删掉这一行)
 */

data class Configuration (
    var tmpDir: File,
    var info: (Any) -> Unit = { println(it) },
    var error: (Any) -> Unit = { println(it) },
    var logger: (String) -> Unit = { println(it) },
    var resolveFileAction: (String) -> File = { File(tmpDir, it) },
    var sinsySynAlpha: Float = 0.55f,
    var sinsyF0shift: Int = 0,
    var sinsyVibpower: Int = 1,
    var sinsyLink: String = "http://sinsy.sp.nitech.ac.jp",
    var miderCodeFormatName: String = "midercode",
    var selfMockeryTime: Long = 7*1000L,
    var selfMockery: Boolean = false,
    var commandTimeout: Long = 60 * 1000L,
    var ffmpegConvertCommand: String = "ffmpeg -i {{input}} -acodec libmp3lame -ab 256k {{output}}",
    var timidityConvertCommand: String = "timidity {{input}} -Ow -o {{output}}",
    var mscoreConvertMidi2Mp3Command: String = "MuseScore3 {{input}} -o {{output}}",
    var mscoreConvertMidi2MSCZCommand: String = "MuseScore3 {{input}} -o {{output}}",
    var mscoreConvertMSCZ2PDFCommand: String = "MuseScore3 {{input}} -o {{output}}",
    var mscoreConvertMSCZ2PNGSCommand: String = "MuseScore3 {{input}} -o {{output}} --trim-image 120",
    var recursionLimit: Int = 50,
    var silkBitsRate: Int = 24000,
    var cache: Boolean = false,
    var formatMode: String = "internal->java-lame",
    var macroUseStrictMode: Boolean = true,
    var debug: Boolean = false,
    var isBlankReplaceWith0: Boolean = false,
    var quality: Int = 64,
    var uploadSize: Long = 1153433L,
    var help: String = "https://github.com/whiterasbk/MiraiMidiProduce/blob/master/README.md"
)