package bot.music.whiter

import whiter.music.mider.dsl.play
import whiter.music.mider.practise.absolutepitch.practise1


fun main(args: Array<String>) {
    play {
//        !toMiderNoteList("F+^B\$C6GFG CE\$")
//        debug()

        toMiderNoteListv2("gE+(=EgDD)Oo~~|A1:A2:A3:A4 (a) ").forEach(::println)

//        note().code
    }
}