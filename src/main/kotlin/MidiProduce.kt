package bot.music.whiter

import io.github.mzdluo123.silk4j.AudioUtils
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.info
import whiter.music.mider.cast
import whiter.music.mider.code.*
import whiter.music.mider.dsl.Dsl2MusicXml
import whiter.music.mider.dsl.fromDslInstance
import whiter.music.mider.xml.LyricInception
import java.io.BufferedInputStream
import java.io.FileFilter
import java.io.InputStream
import java.net.URL

object MidiProduce : KotlinPlugin(
    JvmPluginDescription(
        id = "bot.music.whiter.MidiProduce",
        name = "MidiProduce",
        version = "0.1.7",
    ) {
        author("whiterasbk")
    }
) {
    // todo 1. 出场自带 bgm ( 频率
    // todo 2. 相对音准小测试
    // todo 3. 隔群发送语音, 寻觅知音 (
    // todo 4. 增加乐器( done but to be fix
    // todo 5. 增加力度 done
    // todo 6. mider code for js
    // todo 7. 权限系统, 话说就发个语音有引入命令权限的必要吗 (
    // todo 8. midi 转 mider code

    private val cache = mutableMapOf<String, Message>()
    val tmpDir = resolveDataFile("tmp")

    override fun onEnable() {
        logger.info { "MidiProduce loaded" }

        Config.reload()

        initTmpAndFormatTransfer()

        LyricInception.replace = { it.toPinyin() }

        val process: suspend MessageEvent.() -> Unit = {
            var finishFlag = false
            // todo 改为有概率触发
            if (Config.selfMockery && Math.random() > 0.5) launch {
                delay(Config.selfMockeryTime)
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
                generate()
                oCommandProcess()
            } catch (e: Exception) {
                logger.error(e)
                subject.sendMessage("解析错误>${e::class.simpleName}>" + e.message)
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

    private fun initTmpAndFormatTransfer() {
        if (!tmpDir.exists()) tmpDir.mkdir()

        // unimportant
        val tty = resolveDataFile("2000-years-later.png")
        if (!tty.exists())
            this.javaClass.classLoader.getResourceAsStream("2000-years-later.png")?.let {
                tty.writeBytes(it.readAllBytes())
            } ?: logger.info("can not release 2000-years-later.png")

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
            logger.error("清理缓存失败")
            logger.error(e)
        }

        if (Config.formatMode.contains("silk4j")) {
            try {
                AudioUtils.init(tmpDir)
            } catch (e: Exception) {
                logger.error("silk4j 加载失败, 将无法生成语音")
                logger.error(e)
            }
        }

        if (Config.formatMode.contains("timidity") && Config.timidityConvertCommand.isBlank()) {
            logger.error("timidity 命令未配置, 将无法生成语音(wav)")
        }

        if (Config.formatMode.contains("ffmpeg") && Config.ffmpegConvertCommand.isBlank()) {
            logger.error("ffmpeg 命令未配置, 将无法生成语音(mp3)")
        }
    }

    private suspend fun MessageEvent.oCommandProcess() {
        val oCmdRegex = Regex(">!([\\w@=:&%$#\\->]+)>")
        matchRegex(oCmdRegex) {
            val content = oCmdRegex.matchEntire(it)!!.groupValues[1]
            if (content == "help") {
                subject.sendMessage(Config.help)
            } else if (content.startsWith("formatMode=")) {
                when (val mode = content.replace("formatMode=", "")) {
                    "internal->java-lame->silk4j", "timidity->ffmpeg", "timidity->ffmpeg->silk4j", "internal->java-lame",
                    "timidity->java-lame", "timidity->java-lame->silk4j", "muse-score", "muse-score->silk4j" -> {
                        val before = Config.formatMode
                        Config.formatMode = mode
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
            }
        }
    }

    private suspend fun MessageEvent.generate() {
        var miderCodeFileName = ""
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

            var isRenderingNotation: Boolean
            var isUploadMidi: Boolean
            var notationType: NotationType? = null

            // TODO (notationType == NotationType.PNGS || notationType == null) 此处必为真
            if (Config.cache && msg in cache && (notationType == NotationType.PNGS || notationType == null)) {
                cache[msg]?.let {
                    ifDebug("send from cache")
                    subject.sendMessage(it)
                } ?: throw Exception("启用了缓存但是缓存中没有对应的语音消息")
            } else {

                time {
                    logger.info("sounds begin")

                    val macroConfig = MacroConfigurationBuilder()
                        .recursionLimit(Config.recursionLimit)
                        .loggerError { logger.info(it) }
                        .loggerError { if (Config.macroUseStrictMode) throw it else this@MidiProduce.logger.error(it) }
                        .fetchMethod {
                            if (it.startsWith("http://") || it.startsWith("https://") || it.startsWith("ftp://"))
                                URL(it).openStream().reader().readText()
                            else
                                resolveDataFile(it.replace("file:", "")).readText()
                        }

                    // TODO MiderCodeParserConfiguration.Buider该改改，允许直接 setMacroConfiguration
                    val produceCoreConfiguration = MiderCodeParserConfiguration()

                    produceCoreConfiguration.macroConfiguration = macroConfig.build()
                    produceCoreConfiguration.isBlankReplaceWith0 = Config.isBlankReplaceWith0

                    val produceCoreResult = produceCore(msg, produceCoreConfiguration)

                    /*
                     produceCoreResult的内容:
                     - 若干控制类变量的新值
                     - 得到 miderDSL instance
                     */
                    isRenderingNotation = produceCoreResult.isRenderingNotation
                    isUploadMidi = produceCoreResult.isUploadMidi
                    notationType = produceCoreResult.notationType
                    val midiStream: InputStream = fromDslInstance(produceCoreResult.miderDSL).inStream()

                    if (isRenderingNotation) {
                        // 渲染 乐谱
                        val midi = AudioUtilsGetTempFile("mid")
                        midi.writeBytes(midiStream.readAllBytes())

                        when (notationType) {
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
                    } else if (isUploadMidi && subject is FileSupported) {
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

                        when (this) {
                            is GroupMessageEvent -> {
                                val size = stream.available()
                                if (size > 1024 * 1024) logger.info("文件大于 1m 可能导致语音无法播放, 大于 upload size 时将自动转为文件上传")
                                if (size > Config.uploadSize) {
                                    stream.toExternalResource().use {
                                        group.files.uploadNewFile(
                                            miderCodeFileName + "generate-${System.currentTimeMillis()}.mp3",
                                            it
                                        )
                                    }
                                } else {
                                    stream.toExternalResource().use {
                                        val audio = group.uploadAudio(it)
                                        group.sendMessage(audio)
                                        if (Config.cache) cache[msg] = audio
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
                                        if (Config.cache) cache[msg] = audio
                                    }
                                }
                            }

                            else -> throw Exception("打咩")
                        }
                    }
                }
            }
        }
    }
}

object Config : AutoSavePluginConfig("config") {
    @ValueDescription("sinsy 生成语音的声质, -0.8〜0.8")
    val sinsySynAlpha by value(0.55f)
    @ValueDescription("sinsy 颤音强度, 0.0〜2.0")
    val sinsyF0shift by value(0)
    @ValueDescription("sinsy 俯仰变速, -24〜24")
    val sinsyVibpower by value(1)
    @ValueDescription("sinsy 接口")
    val sinsyLink by value("http://sinsy.sp.nitech.ac.jp")
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
}