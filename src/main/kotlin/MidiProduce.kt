package bot.music.whiter

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.info

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
                group.sendMessage("解析错误: " + e.message)
            }
        }
    }

    private suspend fun GroupMessageEvent.printHelp() {
        if (message.content == ">!help>") {
            group.sendMessage(Config.help)
        }
    }

    private suspend fun GroupMessageEvent.generate() {
        matchRegex(">((g|\\d+b)(;([-+b#]?[A-G](min|maj|major|minor)?))?(;\\d)?)>.*\\s*") { msg ->
            var arrowCount = 0
            var availCount = 0
            var defaultBmp = 80
            var defaultPitch: Byte = 4
            var mode = ""

            msg.forEach {
                if (arrowCount >= 2) return@forEach
                if (it == '>') arrowCount ++
                availCount ++
            }

            val noteList = msg.substring(availCount, msg.length)
            val configPart = msg.substring(0, availCount).replace(">", "").split(";")

            configPart.forEach {
                if (it.matches(Regex("\\d+b"))) {
                    defaultBmp = it.replace("b", "").toInt()
                } else if (it.matches(Regex("[-+b#]?[A-G](min|maj|major|minor)?"))) {
                    mode = it
                } else if (it.matches(Regex("\\d"))) {
                    defaultPitch = it.toByte()
                }
            }

            val stream = midi2mp3Stream(GOOD_QUALITY_BITRATE = Config.quality) {
                bpm = defaultBmp
                if (defaultPitch != 4.toByte()) pitch = defaultPitch

                if (mode.isNotBlank()) {
                    useMode(mode) {
                        !toMiderNoteList(noteList)
                    }
                } else !toMiderNoteList(noteList)
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

object Config : AutoSavePluginConfig("config") {
    val quality by value(64)
    val uploadSize by value(1153433L)
    val help by value("""
命令格式:
>bpm;mode>音符序列
bpm: 速度, 必选, 格式是: 数字+b, 如 120b, 默认可以用 g 代替
mode: 调式, 可选, 格式是(b/#)调式名, 如Cminor, -Emaj

a~g: A4~G4
A~G: A5~G5
0-9: 手动修改音域
 # : 升一个半音
 ${'$'} : 降一个半音
 + : 时值变为原来的两倍
 - : 时值变为原来的一半
 . : 时值变为原来的一点五倍
 : : 两个以上音符组成一个和弦 (目前有bug, 尽量不要使用)
 ~ : 克隆上一个音符
 ^ : 克隆上一个音符, 并升高1度
 v : 克隆上一个音符, 并降低1度
类似的用法还有m-w, n-u, i-!, q-p, s-z,升高或降低度数在^-v的基础上逐步递增或递减
    """.trim())
}