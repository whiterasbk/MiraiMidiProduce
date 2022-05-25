package bot.music.whiter

import java.io.File


fun main(args: Array<String>) {
    val s = midi2mp3Stream {
        !toMiderNoteList("c~g#+~a$~g|")
        debug()
    }

    // Convert("src/test/resources/1.mid", "src/test/resources/ui.wav")
    File("src/test/resources/aya.mp3").writeBytes(s.readAllBytes())
}