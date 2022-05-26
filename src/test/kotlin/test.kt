package bot.music.whiter

import whiter.music.mider.dsl.play
import java.io.File


fun main(args: Array<String>) {
    play {
//        !toMiderNoteList("c~g#+~a$~g|")
        println(toMiderNoteList("011.55'6.6.5"))

        !"A/2+1 A B C/8 C*1"

        debug()
    }

    // Convert("src/test/resources/1.mid", "src/test/resources/ui.wav")
//    File("src/test/resources/aya.mp3").writeBytes(s.readAllBytes())
}