package org.mider.produce.core.utils

import org.mider.produce.core.Configuration
import java.io.File
import java.io.FileFilter
import java.io.InputStream

private fun Configuration.convertUsingConfigCommand(usingCommand: String, inputFile: File, outputFile: File): File {
    val result = usingCommand
        .replace("{{input}}", inputFile.name)
        .replace("{{output}}", outputFile.name)
        .execute(this)

    if (!outputFile.exists() || outputFile.length() == 0L) throw Exception(result.second)

    ifDebug(result.first)

    return outputFile
}

private fun Configuration.convertUsingConfigCommand(usingCommand: String, inputFile: File, outputFileExtension: String): File {
    return convertUsingConfigCommand(usingCommand, inputFile, audioUtilsGetTempFile(outputFileExtension))
}

fun Configuration.timidityConvert(midiFileStream: InputStream): File {
    val midiFile = audioUtilsGetTempFile("mid")
    midiFile.writeBytes(midiFileStream.readAllBytes())
    val outputFile = audioUtilsGetTempFile("wav")

    val result = timidityConvertCommand
        .replace("{{input}}", midiFile.name)
        .replace("{{output}}", outputFile.name)
        .execute(this)

    if (!outputFile.exists() || outputFile.length() == 0L) throw Exception(result.second)
    ifDebug(result.first)

    return outputFile
}

fun Configuration.convert2PDF(midi: File): File {
    return convertUsingConfigCommand(mscoreConvertMSCZ2PDFCommand, midi, "pdf")
}

fun Configuration.convert2PNGS( midi: File): List<File> {
    val outputSample = audioUtilsGetTempFile("png")
    outputSample.writeText("大弦嘈嘈如急雨, 小弦切切如私语")
    convertUsingConfigCommand(mscoreConvertMSCZ2PNGSCommand, midi, outputSample)

    val result = outputSample.parentFile.listFiles(FileFilter {
        it.name.startsWith(outputSample.nameWithoutExtension) && it != outputSample
    }) ?: throw Exception("convert to pngs failed")

    return result.toList().sorted()
}

fun Configuration.ffmpegConvert(midiFileStream: InputStream): File {
    val wavFile = audioUtilsGetTempFile("wav")
    wavFile.writeBytes(midiFileStream.readAllBytes())
    val outputFile = audioUtilsGetTempFile("mp3")

    val result = ffmpegConvertCommand
        .replace("{{input}}", wavFile.name)
        .replace("{{output}}", outputFile.name)
        .execute(this)

    if (!outputFile.exists() || outputFile.length() == 0L) throw Exception(result.second)
    ifDebug(result.first)

    return outputFile
}

fun Configuration.museScoreConvert( midiFileStream: InputStream): File {
    val midiFile = audioUtilsGetTempFile("mid")
    midiFile.writeBytes(midiFileStream.readAllBytes())
    return convertUsingConfigCommand(mscoreConvertMidi2Mp3Command, midiFile, "mp3")
}

fun Configuration.convert2MSCZ(midi: File): File {
    return convertUsingConfigCommand(mscoreConvertMidi2MSCZCommand, midi, "mscz")
}