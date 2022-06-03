package bot.music.whiter

import io.github.mzdluo123.silk4j.AudioUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.info
import whiter.music.mider.dsl.MiderDSL
import whiter.music.mider.dsl.fromDsl
import java.io.FileFilter
import java.io.InputStream
import java.net.URL

object MidiProduce : KotlinPlugin(
    JvmPluginDescription(
        id = "bot.music.whiter.MidiProduce",
        name = "MidiProduce",
        version = "0.1.6",
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

    private val cache = mutableMapOf<String, Message>()
    val tmpDir = resolveDataFile("tmp")

    override fun onEnable() {
        logger.info { "MidiProduce loaded" }

        Config.reload()

        initTmpAndFormatTransfer()

        val process: suspend MessageEvent.() -> Unit = {
            var finishFlag = false
            // todo 改为有概率触发
            if (Config.selfMockery) launch {
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
                    "mid", "midi", "mscz", "png", "pdf", "pcm"
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
                        subject.sendMessage("设置格式转换模式成功, 由 $before 切换为 $mode")
                    }

                    else -> subject.sendMessage("不支持的格式, 请确认设置的值在 以下列表\n" +
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
            }
        }
    }

    private suspend fun MessageEvent.generate() {

        val startRegex = Regex(">(g|f|\\d+b)((;[-+b#]?[A-G](min|maj|major|minor)?)|(;\\d)|(;img)|(;pdf)|(;mscz)|(;midi)|(;i=[a-zA-Z-]+)|(;\\d/\\d))*>")
        val cmdRegex = Regex("${startRegex.pattern}[\\S\\s]+")

        matchRegex(cmdRegex) { msg ->

            var isRenderingNotation = false
            var isUploadMidi = false
            var notationType: NotationType? = null

            if (Config.cache && msg in cache && (notationType == NotationType.PNGS || notationType == null)) {
                cache[msg]?.let {
                    ifDebug("send from cache")
                    subject.sendMessage(it)
                } ?: throw Exception("启用了缓存但是缓存中没有对应的语音消息")
            } else {

                time {
                    logger.info("sounds begin")

                    val noteLists = msg.split(startRegex).toMutableList()
                    noteLists.removeFirst()
                    val configParts = startRegex.findAll(msg).map { it.value.replace(">", "") }.toList()

                    val dslBlock: MiderDSL.() -> Unit = {

                        val macroConfig = MacroConfiguration {

                            recursionLimit(Config.recursionLimit)

                            loggerInfo { logger.info(it) }
                            loggerError {
                                if (Config.macroUseStrictMode) throw it else logger.error(it)
                            }

                            fetchMethod {
                                if (it.startsWith("http://") || it.startsWith("https://") || it.startsWith("ftp://"))
                                    URL(it).openStream().reader().readText()
                                else
                                    resolveDataFile(it.replace("file:", "")).readText()
                            }
                        }
                        val changeBpm = { tempo: Int -> bpm = tempo }
                        val changeOuterProgram = { ins: String -> program = MiderDSL.instrument.valueOf(ins) }
                        val changeTimeSignature = { pair: Pair<Int, Int> -> timeSignature = pair }
                        // todo 怪, 应该每条轨道都能设置才对

                        noteLists.forEachIndexed { index, content ->

                            track {
                                var mode = ""
                                var defaultPitch = 4
                                defaultNoteDuration = 1

                                configParts[index].split(";").forEach {
                                    when (it) {
                                        "f" -> defaultPitch = 3

                                        "midi" -> isUploadMidi = true

                                        "img" -> {
                                            isRenderingNotation = true
                                            notationType = NotationType.PNGS
                                        }

                                        "pdf" -> {
                                            isRenderingNotation = true
                                            notationType = NotationType.PDF
                                        }

                                        "mscz" -> {
                                            isRenderingNotation = true
                                            notationType = NotationType.MSCZ
                                        }

                                        else -> {
                                            if (it.matches(Regex("\\d+b"))) {
                                                changeBpm(it.replace("b", "").toInt())
                                            } else if (it.matches(Regex("[-+b#]?[A-G](min|maj|major|minor)?"))) {
                                                mode = it
                                            } else if (it.matches(Regex("\\d"))) {
                                                defaultPitch = it.toInt()
                                            } else if (it.matches(Regex("\\d/\\d"))) {
                                                val ts = it.split("/")
                                                changeTimeSignature(ts[0].toInt() to ts[1].toInt())
                                            } else if (it.matches(Regex("i=[a-zA-Z-]+"))) {
                                                // 两个都设置下 (
                                                program = MiderDSL.instrument.valueOf(it.replace("i=", ""))
                                                // todo fix
                                                if (!Config.formatMode.contains("muse-score")) {
                                                    changeOuterProgram(it.replace("i=", ""))
                                                    ifDebug("set outer program to $program")
                                                }
                                                ifDebug("set program to $program")
                                            }
                                        }
                                    }
                                }

                                val sequence = macro(content, macroConfig)

                                val isStave =
                                    Regex("[c-gaA-G]").find(sequence) != null || Regex("(\\s*b\\s*)+").matches(sequence)

                                val rendered = toInMusicScoreList(
                                    sequence.let {
                                        if (isStave && Config.isBlankReplaceWith0) it else
                                            it.trim().replace(Regex("( {2}| \\| )"), "0")
                                    },
                                    isStave = isStave,
                                    pitch = defaultPitch, useMacro = false
                                )

                                ifUseMode(mode) {
                                    val stander = toMiderStanderNoteString(rendered)
                                    if (stander.isNotBlank()) !stander
                                }

                                ifDebug { logger.info("track: ${index + 1}"); debug() }
                            }

                        }
                    }

                    val midiStream: InputStream = fromDsl(dslBlock).inStream()

                    if (isRenderingNotation) {
                        // 渲染 乐谱
                        if (isRenderingNotation) {
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

                        val stream = generateAudioStreamByFormatMode(midiStream)
                        when (this) {

                            is GroupMessageEvent -> {
                                val size = stream.available()
                                if (size > 1024 * 1024) logger.info("文件大于 1m 可能导致语音无法播放, 大于 upload size 时将自动转为文件上传")
                                if (size > Config.uploadSize) {
                                    stream.toExternalResource().use {
                                        group.files.uploadNewFile(
                                            "generate-${System.currentTimeMillis()}.mp3",
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

    private fun MiderDSL.ifUseMode(mode: String, block: MiderDSL.()-> Unit) {
        if (mode.isNotBlank()) {
            useMode(mode) {
                block()
            }
        } else block()
    }
}

enum class NotationType {
    PNGS, MSCZ, PDF
}

object Config : AutoSavePluginConfig("config") {

    @ValueDescription("2000year")
    val selfMockeryTime by value(7 * 1000L)
    @ValueDescription("is2000year(")
    val selfMockery by value(true)

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

    @ValueDescription("格式转换输出模式, 可选的有: \n" +
            "internal->java-lame(默认)\n" +
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
    val help by value("""
# 命令格式 (一个命令代表一条轨道)
>bpm[;mode][;pitch][;midi][;img][;pdf][;mscz]>音名序列 | 唱名序列
bpm: 速度, 必选, 格式是: 数字 + b, 如 120b, 默认可以用 g 或者 f 代替
mode: 调式, 可选, 格式是 b/#/-/+ 调式名, 如 Cminor, -Emaj, bC
pitch: 音域(音高), 可选, 默认为 4
midi: 是否仅上传 midi 文件, 可选
img: 是否仅上传 png 格式的乐谱
pdf: 是否仅上传 pdf 文件, 可选
mscz: 是否仅上传 mscz 文件, 可选
音名序列的判断标准是序列里是否出现了 c~a 或 C~B 中任何一个字符

# 示例
>g>1155665  4433221  5544332  5544332
等同于
>g>ccggaag+ffeeddc+ggffeed+ggffeed
等同于
>g>c~g~^~v+f~v~v~v+(repeat 2:g~v~v~v+) (酌情使用

# 公用规则 (如无特殊说明均使用在唱名或音名后, 并可叠加使用)
 # : 升一个半音, 使用在音名或唱名前
 ${'$'} : 降一个半音, 使用在音名或唱名前
 + : 时值变为原来的两倍
 - : 时值变为原来的一半
 . : 时值变为原来的一点五倍
 : : 两个以上音符组成一个和弦
 ~ : 克隆上一个音符
 ^ : 克隆上一个音符, 并升高 1 度
 v : 克隆上一个音符, 并降低 1 度
 ↑ : 升高一个八度
 ↓ : 降低一个八度
 & : 还原符号
类似的用法还有 m-w, n-u, i-!, q-p, s-z 升高或降低度数在 ^-v 的基础上逐步递增或递减

# 如果是音名序列则以下规则生效
a~g: A4~G4
A~G: A5~G5
 O : 二分休止符 
 o : 四分休止符 
0-9: 手动修改音域

# 如果是唱名序列则以下规则生效
1~7: C4~B4
 0 : 四分休止符
 i : 升高一个八度
 ! : 降低一个八度
 b : 降低一个半音, 使用在唱名前
 * : 后接一个一位数字表示重复次数
 
# 宏
目前可用的宏有
1. (def symbol=note sequence) 定义一个音符序列
2. (def symbol:note sequence) 定义一个音符序列, 并在此处展开
3. (=symbol) 展开 symbol 对应音符序列
4. (include path) 读取 path 代表的资源并展开
5. (repeat times: note sequence) 将音符序列重复 times 次
6. (ifdef symbol: note sequence) 如果定义了 symbol 则展开
7. (if!def symbol: note sequence) 如果未定义 symbol 则展开
8. (macro name param1[,params]: note sequence @[param1]) 定义宏
9. (!name arg1[,arg2]) 展开宏
目前宏均不可嵌套使用
""".trim())
}