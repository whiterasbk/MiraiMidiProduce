package bot.music.whiter

import io.github.mzdluo123.silk4j.AudioUtils
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.info
import whiter.music.mider.dsl.MiderDSL
import java.io.FileFilter
import java.io.InputStream
import java.net.URL

object MidiProduce : KotlinPlugin(
    JvmPluginDescription(
        id = "bot.music.whiter.MidiProduce",
        name = "MidiProduce",
        version = "0.1.4",
    ) {
        author("whiterasbk")
    }
) {

    val tmpDir = resolveDataFile("tmp")

    override fun onEnable() {
        logger.info { "MidiProduce loaded" }

        Config.reload()

        if (!tmpDir.exists()) tmpDir.mkdir()

        try {
            tmpDir.listFiles(FileFilter {
                it.extension == "so" ||
                it.extension == "dll" ||
                it.extension == "lib" ||
                it.extension == "mp3" ||
                it.extension == "silk" ||
                it.extension == "wave" ||
                it.extension == "wav" ||
                it.extension == "amr" ||
                it.extension == "mid" ||
                it.extension == "midi" ||
                it.extension == "pcm"
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

        globalEventChannel().subscribeAlways<GroupMessageEvent> {
            try {
                generate()
                printHelp()
            } catch (e: Exception) {
                logger.error(e)
                group.sendMessage("解析错误>${e::class.simpleName}>" + e.message)
            }
        }

        globalEventChannel().subscribeAlways<FriendMessageEvent> {
            try {
                generate()
                printHelp()
            } catch (e: Exception) {
                logger.error(e)
                friend.sendMessage("解析错误>${e::class.simpleName}>" + e.message)
            }
        }
    }

    private suspend fun MessageEvent.printHelp() {
        if (message.content == ">!help>") {
            subject.sendMessage(Config.help)
        }
    }

    private suspend fun MessageEvent.generate() {

        val startRegex = Regex(">((g|f|\\d+b)(;([-+b#]?[A-G](min|maj|major|minor)?))?(;\\d)?(;vex|vex&au)?)>")
        val cmdRegex = Regex("${startRegex.pattern}[\\S\\s]+")

        matchRegex(cmdRegex) { msg ->
            time {
                logger.info("sounds begin")

                val noteLists = msg.split(startRegex).toMutableList()
                noteLists.removeFirst()
                val configParts = startRegex.findAll(msg).map { it.value.replace(">", "") }.toList()

                val stream: InputStream = generateStreamByFormatMode {

                    val macroConfig = MacroConfiguration {

                        recursionLimit(50)

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

                    noteLists.forEachIndexed { index, content ->

                        track {
                            var mode = ""
                            var defaultPitch = 4

                            defaultNoteDuration = 1

                            configParts[index].split(";").forEach {
                                if (it == "f") {
                                    defaultPitch = 3
                                } else if (it.matches(Regex("\\d+b"))) {
                                    changeBpm(it.replace("b", "").toInt())
                                } else if (it.matches(Regex("[-+b#]?[A-G](min|maj|major|minor)?"))) {
                                    mode = it
                                } else if (it.matches(Regex("\\d"))) {
                                    defaultPitch = it.toInt()
                                } else if (it.matches(Regex("vex|wex&au"))) {
                                    // todo 渲染乐谱
                                }
                            }

                            val sequence = macro(content, macroConfig)

                            val isStave = Regex("[c-gaA-G]").find(sequence) != null || Regex("(\\s*b\\s*)+").matches(sequence)

                            val rendered = toInMusicScoreList(sequence.let {
                                if (isStave && Config.isBlankReplaceWith0) it else
                                    it.trim().replace(Regex("( {2}| \\| )"),"0")
                                },
                                isStave = isStave,
                                pitch = defaultPitch, useMacro = false)

                            ifUseMode(mode) {
                                val stander = toMiderStanderNoteString(rendered)
                                if (stander.isNotBlank()) !stander
                            }

                            // 渲染 乐谱

                            ifDebug { logger.info("track: ${index + 1}"); debug() }
                        }

                    }
                }

                when (this) {
                    is GroupMessageEvent -> {
                        if (stream.available() > Config.uploadSize) {
                            stream.toExternalResource().use { group.files.uploadNewFile("generate.mp3", it) }
                        } else {
                            stream.toExternalResource().use {
                                group.sendMessage(group.uploadAudio(it))
                            }
                        }
                    }

                    is FriendMessageEvent -> {
                        if (stream.available() > Config.uploadSize) {
                            friend.sendMessage("文件过大无法上传")
                        } else {
                            stream.toExternalResource().use {
                                friend.sendMessage(friend.uploadAudio(it))
                            }
                        }
                    }

                    else -> throw Exception("打咩")
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

object Config : AutoSavePluginConfig("config") {

    @ValueDescription("ffmpeg 转换命令")
    val ffmpegConvertCommand by value("ffmpeg -i {{input}} -acodec libmp3lame -ab 256k {{output}}")
    @ValueDescription("timidity 转换命令")
    val timidityConvertCommand by value("timidity {{input}} -Ow -o {{output}}")

    @ValueDescription("silk 比特率(吧")
    val silkBitsRate by value(24000)

    @ValueDescription("格式转换输出 可选的有: \n" +
            "internal->java-lame(默认)\n" +
            "internal->java-lame->silk4j\n" +
            "timidity->ffmpeg\n" +
            "timidity->ffmpeg->silk4j\n" +
            "timidity->java-lame\n" +
            "timidity->java-lame->silk4j\n"
    )
    val formatMode by value("internal->java-lame")
    @ValueDescription("宏是否启用严格模式")
    val macroUseStrictMode by value(true)
    @ValueDescription("是否启用调试")
    val debug by value(true)
    @ValueDescription("是否启用空格替换")
    val isBlankReplaceWith0 by value(true)
    @ValueDescription("量化深度 理论上越大生成 mp3 的质量越好, java-lame 给出的值是 256")
    val quality by value(64)
    @ValueDescription("超过这个大小则自动改为文件上传")
    val uploadSize by value(1153433L)
    @ValueDescription("帮助信息 (更新版本时记得要删掉这一行)")
    val help by value("""
# 命令格式 (一个命令代表一条轨道)
>bpm[;mode;pitch]>音名序列|简谱序列
bpm: 速度, 必选, 格式是: 数字 + b, 如 120b, 默认可以用 g 或者 f 代替
mode: 调式, 可选, 格式是 b/#/-/+ 调式名, 如 Cminor, -Emaj, bC
pitch: 音域(音高), 可选, 默认为 4
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
5. (repeat time: note sequence) 将音符序列重复 times 次
6. (ifdef symbol: note sequence) 如果定义了 symbol 则展开
7. (if!def symbol: note sequence) 如果未定义 symbol 则展开
8. (macro name param1[,params]: note sequence @[param1]) 定义宏
9. (!name arg1[,arg2]) 展开宏
目前宏均不可嵌套使用
""".trim())
}