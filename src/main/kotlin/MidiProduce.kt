package bot.music.whiter

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.info
import whiter.music.mider.dsl.MiderDSL
import java.net.URL

object MidiProduce : KotlinPlugin(
    JvmPluginDescription(
        id = "bot.music.whiter.MidiProduce",
        name = "MidiProduce",
        version = "0.1",
    ) {
        author("whiterasbk")
    }
) {
    override fun onEnable() {
        logger.info { "MidiProduce loaded" }
        Config.reload()

        globalEventChannel().subscribeAlways<GroupMessageEvent> {
            try {
                generate()
                printHelp()
            } catch (e: Exception) {
                logger.error(e)
                group.sendMessage("解析错误>${e::class.simpleName}: " + e.message)
            }
        }
    }

    private suspend fun GroupMessageEvent.printHelp() {
        if (message.content == ">!help>") {
            group.sendMessage(Config.help)
        }
    }

    private suspend fun GroupMessageEvent.generate() {

        val startRegex = Regex(">((g|f|\\d+b)(;([-+b#]?[A-G](min|maj|major|minor)?))?(;\\d)?(;vex|vex&au)?)>")
        val cmdRegex = Regex("${startRegex.pattern}[\\S\\s]+")

        matchRegex(cmdRegex) { msg ->
            time {
                logger.info("sounds begin")

                val noteLists = msg.split(startRegex).toMutableList()
                noteLists.removeFirst()
                val configParts = startRegex.findAll(msg).map { it.value.replace(">", "") }.toList()

                val stream = midi2mp3Stream(GOOD_QUALITY_BITRATE = Config.quality) {

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

                            val isStave = Regex("[c-gC-GaA]").find(sequence) != null || Regex("(\\s*b\\s*)+").matches(sequence)

                            val rendered = toInMusicScoreList(sequence.let {
                                if (isStave) it else
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

                if (stream.available() > Config.uploadSize) {
                    stream.toExternalResource().use { group.files.uploadNewFile("generate.mp3", it) }
                } else {
                    stream.toExternalResource().use {
                        group.sendMessage(group.uploadAudio(it))
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

object Config : AutoSavePluginConfig("config") {
    @ValueDescription("宏是否启用严格模式")
    val macroUseStrictMode by value(true)
    @ValueDescription("是否启用调试")
    val debug by value(true)
    @ValueDescription("生成 mp3 的质量")
    val quality by value(64)
    @ValueDescription("超过这个大小则自动改为文件上传")
    val uploadSize by value(1153433L)
    @ValueDescription("帮助信息 (更新版本时记得要删掉这一行)")
    val help by value("""
# 命令格式 (一个命令代表一条轨道)
>bpm[;mode;pitch]>音名序列|简谱序列
bpm: 速度, 必选, 格式是: 数字+b, 如 120b, 默认可以用 g 或者 f 代替
mode: 调式, 可选, 格式是 b/#/-/+ 调式名, 如 Cminor, -Emaj, bC
pitch: 音域(音高), 可选, 默认为 4
音名序列的判断标准是序列里是否出现了 c~a 或 C~B 中任何一个字符

# 示例
>g>1155665  4433221  5544332  5544332
等同于
>g>ccggaag+ffeeddc+ggffeed+ggffeed

# 公用规则 (如无特殊说明均使用在唱名或音名后, 并可叠加使用)
 # : 升一个半音, 使用在音名或唱名前
 ${'$'} : 降一个半音, 使用在音名或唱名前
 + : 时值变为原来的两倍
 - : 时值变为原来的一半
 . : 时值变为原来的一点五倍
 : : 两个以上音符组成一个和弦
 ~ : 克隆上一个音符
 ^ : 克隆上一个音符, 并升高1度
 v : 克隆上一个音符, 并降低1度
 ↑ : 升高一个八度
 ↓ : 降低一个八度
 & : 还原符号
类似的用法还有m-w, n-u, i-!, q-p, s-z,升高或降低度数在^-v的基础上逐步递增或递减

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