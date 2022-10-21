package org.mider.produce.core.utils

import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.ExecuteWatchdog
import org.apache.commons.exec.PumpStreamHandler
import org.mider.produce.core.Configuration
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

suspend fun String.matchRegex(reg: Regex, block: suspend (String) -> Unit) {
    if (this.matches(reg)) {
        block(this)
    }
}

fun String.execute(config: Configuration, charset: Charset = Charset.forName("utf-8")): Pair<String, String> {
    //接收正常结果流
    val outputStream = ByteArrayOutputStream()
    //接收异常结果流
    val errorStream = ByteArrayOutputStream()
    val commandline: CommandLine = CommandLine.parse(this)
    val exec = DefaultExecutor()
    exec.workingDirectory = config.tmpDir
    exec.setExitValues(null)
    val watchdog = ExecuteWatchdog(config.commandTimeout)
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