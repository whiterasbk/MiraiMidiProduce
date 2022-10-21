package org.mider.produce.bot

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.mider.produce.bot.utils.sendAudioMessage
import org.mider.produce.core.Configuration
import org.mider.produce.core.generate
import org.mider.produce.core.utils.ifDebug
import org.mider.produce.core.utils.matchRegex
import whiter.music.mider.cast
import whiter.music.mider.code.MiderCodeParserConfiguration
import whiter.music.mider.code.NotationType
import whiter.music.mider.code.startRegex

suspend fun MessageEvent.handle(coreCfg: Configuration, miderCfg: MiderCodeParserConfiguration) {
    var miderCodeFileName: String? = null
    val underMsg = if (this is GroupMessageEvent && FileMessage in message) {
        val fileMessage = message.find { it is FileMessage }.cast<FileMessage>()
        if (fileMessage.name.endsWith("." + BotConfiguration.miderCodeFormatName)) {
            miderCodeFileName = fileMessage.name.split(".")[0] + "-"
            val url = fileMessage.toAbsoluteFile(group)?.getUrl()
            val client = HttpClient(OkHttp)
            client.get(url ?: throw Exception("current file: ${fileMessage.name} does not exist")).bodyAsText()
        } else message.content
    } else message.content

    val cmdRegex = Regex("${startRegex.pattern}[\\S\\s]+")

    underMsg.matchRegex(cmdRegex) { msg ->
        if (coreCfg.cache && msg in MiderBot.cache) {
            MiderBot.cache[msg]?.let {
                coreCfg.ifDebug("send from cache")
                subject.sendMessage(it)
            } ?: throw Exception("启用了缓存但是缓存中没有对应的语音消息")
        } else {
            val (result, generated) = coreCfg.generate(msg, miderCfg)
            val (stream, desc) = generated[0]

            when {
                result.isUploadMidi -> stream.toExternalResource().use {
                    (subject as FileSupported).files.uploadNewFile(
                        "generate-${System.currentTimeMillis()}.mid",
                        it
                    )
                }

                result.isRenderingNotation -> {
                    when (result.notationType) {
                        NotationType.PNGS -> {
                            val chain = buildMessageChain {
                                generated.forEach { pair ->
                                    val (png, _) = pair
                                    png.toExternalResource().use {
                                        val img = subject.uploadImage(it)
                                        subject.sendMessage(img)
                                        delay(50)
                                        +img
                                    }
                                }
                            }
                            if (coreCfg.cache) MiderBot.cache[msg] = chain
                        }

                        NotationType.PDF -> {
                            if (subject is FileSupported) {
                                stream.toExternalResource().use {
                                    (subject as FileSupported).files.uploadNewFile(desc, it)
                                }
                            } else subject.sendMessage("打咩")
                        }

                        NotationType.MSCZ -> {
                            if (subject is FileSupported) {
                                stream.toExternalResource().use {
                                    (subject as FileSupported).files.uploadNewFile(desc, it)
                                }
                            } else subject.sendMessage("打咩")
                        }

                        else -> throw Exception("plz provide the output format")
                    }
                }

                else -> sendAudioMessage(coreCfg, msg, stream, miderCodeFileName)
            }
        }
    }
}
