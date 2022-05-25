package bot.music.whiter

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.info
import whiter.music.mider.dsl.apply

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
            } catch (e: Exception) {
                logger.error(e)
                group.sendMessage("解析错误: " +  e.message)
            }
        }
    }

    suspend fun GroupMessageEvent.generate() {
        matchRegex(">((g|bpm\\d+)(;([-+]?[A-G](min|maj)))?(;\\d)?)>.*\\s*") { msg ->
            var arrowCount = 0
            var availCount = 0
            var defaultBmp = 80
            var minOrMaj = 0
            var mode = 'C'
            var flap = '&'
            var defaultPitch: Byte = 4

//            val midiFile = resolveDataFile("tmp.mid")
//            val outputFile = resolveDataFile("output.mp3")
//            val tmpWav = resolveDataFile("tmp.wav")

            msg.forEach {
                if (arrowCount >= 2) return@forEach
                if (it == '>') arrowCount ++
                availCount ++
            }

            val noteList = msg.substring(availCount, msg.length)
            val configPart = msg.substring(0, availCount).replace(">", "").split(";")

            configPart.forEach {
                if (it.matches(Regex("bpm\\d+"))) {
                    defaultBmp = it.replace("bpm", "").toInt()
                } else if (it.matches(Regex("[-+]?[A-G](min|maj)"))) {
                    val next = if (it.first() in "-+") {
                        flap = it.first()
                        it.substring(1, it.length)
                    } else {
                        it
                    }

                    if (next.first() !in "ABCDEFG") throw Exception("not in ABCDEFG")

                    mode = next.first()
                    val mm = next.substring(1, next.length)
                    minOrMaj = when(mm) {
                        "maj" -> 0
                        "min" -> 1
                        else -> 0
                    }
                } else if (it.matches(Regex("\\d"))) {
                    defaultPitch = it.toByte()
                }
            }

            val stream = midi2mp3Stream(GOOD_QUALITY_BITRATE = Config.quality) {
                bpm = defaultBmp
                if (defaultPitch != 4.toByte()) pitch = defaultPitch

                logger.info("$mode>$flap")

                when(mode) {
                    'A' -> {
                        if (flap == '+') {
                            (+A)(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        } else if (flap == '-') {
                            (-A)(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        } else {
                            A(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        }
                    }
                    'B' -> {
                        if (flap == '+') {
                            (+B)(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        } else if (flap == '-') {
                            (-B)(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        } else {
                            B(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        }
                    }
                    'C' -> {
                        if (flap == '+') {
                            (+C)(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        } else if (flap == '-') {
                            (-C)(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        } else {
                            C(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        }
                    }
                    'D' -> {
                        if (flap == '+') {
                            (+D)(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        } else if (flap == '-') {
                            (-D)(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        } else {
                            D(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        }
                    }
                    'E' -> {
                        if (flap == '+') {
                            (+E)(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        } else if (flap == '-') {
                            (-E)(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        } else {
                            E(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        }
                    }
                    'F' -> {
                        if (flap == '+') {
                            (+F)(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        } else if (flap == '-') {
                            (-F)(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        } else {
                            F(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        }
                    }
                    'G' -> {
                        if (flap == '+') {
                            (+G)(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        } else if (flap == '-') {
                            (-G)(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        } else {
                            G(minOrMaj) {
                                !toMiderNoteList(noteList)
                            }
                        }
                    }
                    else -> !toMiderNoteList(noteList)
                }
            }

//            if (outputFile.exists()) outputFile.delete()
//            if (tmpWav.exists()) tmpWav.delete()

//            val cmd = "timidity ${midiFile.name} -Ow -o - | ffmpeg -y -i - -acodec libmp3lame -ab 256k ${outputFile.name}"

//            val usingTimidity = "timidity ${midiFile.name} -Ow -o tmp.wav"
//            val usingFfmpeg = "ffmpeg -y -i tmp.wav -acodec libmp3lame -ab 256k ${outputFile.name}"

//            usingTimidity.runCommand(midiFile.parentFile)
//            usingFfmpeg.runCommand(midiFile.parentFile)



//            if (outputFile.exists()) {
                if (stream.available() > 1153433L) {
                    stream.toExternalResource().use { group.files.uploadNewFile("generate.mp3", it) }
                } else {
                    stream.toExternalResource().use {
                        group.sendMessage(group.uploadAudio(it))
                    }
                }
//            } else group.sendMessage(QuoteReply(message) + "生成失败")
        }
    }
}

object Config : AutoSavePluginConfig("config") {
    val quality by value(64)
}