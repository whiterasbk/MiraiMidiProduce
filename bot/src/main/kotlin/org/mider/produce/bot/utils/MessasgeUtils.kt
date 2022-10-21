package org.mider.produce.bot.utils

import net.mamoe.mirai.contact.AudioSupported
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.mider.produce.bot.MiderBot
import org.mider.produce.core.Configuration
import org.mider.produce.core.utils.*
import whiter.music.mider.code.produceCore
import whiter.music.mider.dsl.fromDslInstance
import java.io.InputStream

suspend fun MessageEvent.matchRegex(reg: Regex, block: suspend (String) -> Unit)
    = message.content.matchRegex(reg, block)

suspend fun MessageEvent.matchRegex(reg: String, block: suspend (String) -> Unit)
    = matchRegex(Regex(reg), block)

suspend fun MessageEvent.sendAudioMessage(Config: Configuration, origin: String, stream: InputStream, attachUploadFileName: String? = null) {
    when (this) {
        is GroupMessageEvent -> {
            val size = stream.available()
            if (size > 1024 * 1024) MiderBot.logger.info("文件大于 1m 可能导致语音无法播放, 大于 upload size 时将自动转为文件上传")
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
                    if (Config.cache) MiderBot.cache[origin] = audio
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
                    if (Config.cache) MiderBot.cache[origin] = audio
                }
            }
        }

        else -> throw Exception("打咩")
    }
}

suspend fun AudioSupported.sendMiderCode(cfg: Configuration, code: String) {
    val result = produceCore(code, MiderBot.produceCoreConfiguration)
    val midiStream = fromDslInstance(result.miderDSL).inStream()
    val audioStream = cfg.generateAudioStreamByFormatMode(midiStream)
    audioStream.toExternalResource().use {
        val audio = uploadAudio(it)
        sendMessage(audio)
    }
}
