package org.mider.produce.bot

import io.github.mzdluo123.silk4j.AudioUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.info
import org.mider.produce.bot.game.gameStart
import org.mider.produce.bot.utils.matchRegex
import org.mider.produce.core.Configuration
import org.mider.produce.core.initTmpAndFormatTransfer
import org.mider.produce.core.utils.toPinyin
import whiter.music.mider.code.MacroConfiguration
import whiter.music.mider.code.MacroConfigurationBuilder
import whiter.music.mider.code.MiderCodeParserConfiguration
import whiter.music.mider.xml.LyricInception
import java.net.URL

object Bot : KotlinPlugin(
    JvmPluginDescription(
        id = "org.mider.produce.bot",
        name = "Bot",
        version = "0.1.8",
    ) {
        author("whiterasbk")
    }
) {
    // todo 1. 出场自带 bgm ( 频率
    // todo 2. 相对音准小测试
    // todo 3. 隔群发送语音, 寻觅知音 (
    // todo 4. 增加乐器( done
    // todo 5. 增加力度 done
    // todo 6. mider code for js
    // todo 7. 权限系统, 话说就发个语音有引入命令权限的必要吗 (
    // todo 8. midi 转 mider code

    val cache = mutableMapOf<String, Message>()
    private val tmpDir = resolveDataFile("tmp")
    private lateinit var macroConfig: MacroConfiguration
    // TODO MiderCodeParserConfiguration.Buider该改改，允许直接 setMacroConfiguration
    val produceCoreConfiguration = MiderCodeParserConfiguration()

    override fun onEnable() {
        logger.info { "MidiProduce loaded" }

        BotConfiguration.reload()

        val cfg = Configuration(tmpDir)
        cfg.initTmpAndFormatTransfer()

        cfg.info = {
            logger.info(it.toString())
        }

        cfg.error = {
            if (it is Throwable)
                logger.error(it)
            else logger.error(it.toString())
        }

        BotConfiguration.copy(cfg)

        LyricInception.replace = { it.toPinyin() }

        macroConfig = MacroConfigurationBuilder()
            .recursionLimit(BotConfiguration.recursionLimit)
            .loggerError { logger.info(it) }
            .loggerError { if (BotConfiguration.macroUseStrictMode) throw it else this@Bot.logger.error(it) }
            .fetchMethod {
                if (it.startsWith("http://") || it.startsWith("https://") || it.startsWith("ftp://"))
                    URL(it).openStream().reader().readText()
                else
                    resolveDataFile(it.replace("file:", "")).readText()
            }
            .build()

        produceCoreConfiguration.macroConfiguration = macroConfig
        produceCoreConfiguration.isBlankReplaceWith0 = BotConfiguration.isBlankReplaceWith0

        val process: suspend MessageEvent.() -> Unit = {
            var finishFlag = false
            // todo 改为有概率触发
            if (BotConfiguration.selfMockery && Math.random() > 0.5) launch {
                delay(BotConfiguration.selfMockeryTime)
                // 开始嘲讽
                if (!finishFlag) {
                    subject.sendMessage("...")
                    delay(50)
                    val tty = resolveDataFile("2000-years-later.png")
                    if (tty.exists()) {
                        tty.toExternalResource().use {
                            subject.sendMessage(subject.uploadImage(it))
                        }
                    } else subject.sendMessage("10 years later.")
                }
            }

            try {
                handle(cfg, produceCoreConfiguration)
                oCommandProcess()
            } catch (e: Exception) {
                logger.error(e)
                subject.sendMessage("发生错误>类型:${e::class.simpleName}>" + e.message)
            } finally {
                finishFlag = true
            }
        }

        val botEvent = globalEventChannel().filter { it is BotEvent }

        botEvent.subscribeAlways<GroupMessageEvent>{
            process()
        }

        botEvent.subscribeAlways<FriendMessageEvent> {
            process()
        }
    }

    private suspend fun MessageEvent.oCommandProcess() {
        val oCmdRegex = Regex(">!([\\w@=:&%$#\\->]+)>")
        matchRegex(oCmdRegex) {
            val content = oCmdRegex.matchEntire(it)!!.groupValues[1]
            if (content == "help") {
                subject.sendMessage(BotConfiguration.help)
            } else if (content.startsWith("formatMode=")) {
                when (val mode = content.replace("formatMode=", "")) {
                    "internal->java-lame->silk4j", "timidity->ffmpeg", "timidity->ffmpeg->silk4j", "internal->java-lame",
                    "timidity->java-lame", "timidity->java-lame->silk4j", "muse-score", "muse-score->silk4j" -> {
                        val before = BotConfiguration.formatMode
                        BotConfiguration.formatMode = mode
                        if (mode.contains("silk4j")) AudioUtils.init(tmpDir)
                        cache.clear()
                        subject.sendMessage("设置生成模式成功, 由 $before 切换为 $mode")
                    }

                    else -> subject.sendMessage("不支持的模式, 请确认设置的值在以下列表\n" +
                            "internal->java-lame\n" +
                            "internal->java-lame->silk4j,\n" +
                            "timidity->ffmpeg,\n" +
                            "timidity->ffmpeg->silk4j,\n" +
                            "timidity->java-lame,\n" +
                            "timidity->java-lame->silk4j,\n" +
                            "muse-score,\n" +
                            "muse-score->silk4j")
                }
            } else if (content == "clear-cache") {
                cache.clear()
                subject.sendMessage("cache cleared")
            } else if (content.startsWith("game-start:")) {
                gameStart(content.replaceFirst("game-start:", ""))
            }
        }
    }

/*    private suspend fun MessageEvent.generate() {
        var miderCodeFileName: String? = null
        val underMsg = if (this is GroupMessageEvent && FileMessage in message) {
            val fileMessage = message.find { it is FileMessage }.cast<FileMessage>()
            if (fileMessage.name.endsWith("." + Config.miderCodeFormatName)) {
                miderCodeFileName = fileMessage.name.split(".")[0] + "-"
                val url = fileMessage.toAbsoluteFile(group)?.getUrl()
                val client = HttpClient(OkHttp)
                client.get(url ?: throw Exception("current file: ${fileMessage.name} does not exist"))
            } else message.content
        } else message.content

        val cmdRegex = Regex("${startRegex.pattern}[\\S\\s]+")

        underMsg.matchRegex(cmdRegex) { msg ->
            if (Config.cache && msg in cache) {
                cache[msg]?.let {
                    ifDebug("send from cache")
                    subject.sendMessage(it)
                } ?: throw Exception("启用了缓存但是缓存中没有对应的语音消息")
            } else {

                time {
                    logger.info("sounds begin")

                    val produceCoreResult = produceCore(msg, produceCoreConfiguration)


//                     produceCoreResult的内容:
//                     - 若干控制类变量的新值
//                     - 得到 miderDSL instance

                    val midiStream: InputStream = fromDslInstance(produceCoreResult.miderDSL).inStream()

                    if (produceCoreResult.isRenderingNotation) {
                        // 渲染 乐谱
                        val midi = AudioUtilsGetTempFile("mid")
                        midi.writeBytes(midiStream.readAllBytes())

                        when (produceCoreResult.notationType) {
                            NotationType.PNGS -> {
                                val chain = buildMessageChain {
                                    convert2PNGS(midi).forEach { png ->
                                        png.toExternalResource().use {
                                            val img = subject.uploadImage(it)
                                            subject.sendMessage(img)
                                            delay(50)
                                            +img
                                        }
                                    }
                                }
                                if (Config.cache) cache[msg] = chain
                            }

                            NotationType.PDF -> {
                                if (subject is FileSupported) {
                                    val pdf = convert2PDF(midi)
                                    pdf.toExternalResource().use {
                                        (subject as FileSupported).files.uploadNewFile(pdf.name, it)
                                    }
                                } else subject.sendMessage("打咩")
                            }

                            NotationType.MSCZ -> {
                                if (subject is FileSupported) {
                                    val mscz = convert2MSCZ(midi)
                                    mscz.toExternalResource().use {
                                        (subject as FileSupported).files.uploadNewFile(mscz.name, it)
                                    }
                                } else subject.sendMessage("打咩")
                            }

                            else -> throw Exception("plz provide the output format")
                        }
                    } else if (produceCoreResult.isUploadMidi && subject is FileSupported) {
                        // 上传 midi
                        midiStream.toExternalResource().use {
                            (subject as FileSupported).files.uploadNewFile(
                                "generate-${System.currentTimeMillis()}.mid",
                                it
                            )
                        }
                    } else {
                        val stream = if (produceCoreResult.isSing) {
                            val xmlFile = AudioUtilsGetTempFile("xml")
                            val dsl2MusicXml = Dsl2MusicXml(produceCoreResult.miderDSL)
                            dsl2MusicXml.save(xmlFile)

                            val singer = selectSinger(produceCoreResult.singSong!!.first to produceCoreResult.singSong!!.second)
                            val sinsyCfg = SinsyConfig(singer.second, singer.first)
                            val after = sinsy(xmlFile.absolutePath, sinsyCfg)
                            generateAudioStreamByFormatModeFromWav(BufferedInputStream(after))
                        } else {
                            generateAudioStreamByFormatMode(midiStream)
                        }

                        sendAudioMessage(msg, stream, miderCodeFileName)
                    }
                }
            }
        }
    }*/


}

