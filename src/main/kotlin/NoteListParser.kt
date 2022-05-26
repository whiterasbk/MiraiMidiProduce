package bot.music.whiter

import java.util.*

fun toMiderNoteList(str: String, defaultPitch: Int = 4): String {
    val list = mutableListOf<Note>()
    val stack = Stack<Char>()
    val cha = str + 'a'
    var lastNote: Note? = null

    val fromPreviousChars = "^vi~!mwnupqsz".toCharArray()
    val notePattenChars = ('a'..'g') + ('A'..'G')
    val noteModifyChars = ('0'..'9') + "$:.#+-"

    cha.forEach { char ->
        if (char !in fromPreviousChars) {
            if (char in notePattenChars) {
                // println("iLLLg: $it")
                if (!stack.empty()) {
                    var r = ""
                    while (!stack.empty()) {
                        r += stack.pop()
                    }

                    // println("inaagg: ${r.reversed()}")

                    if (r.reversed()[0] in fromPreviousChars) {
                        val nc = convertPrevious(r.reversed(), lastNote!!)
                        lastNote = nc
                        list.add(nc)
                    } else {
                        val n = toNote(r.reversed(), defaultPitch)
                        lastNote = n
                        list.add(n)
                    }


                }

                stack.push(char)
            } else {

                if (char in noteModifyChars) {
                    stack.push(char)
                }
            }
        } else {

            // println(">>$it  $last_note")

            if (!stack.empty()) {
                var r = ""
                while (!stack.empty()) {
                    r += stack.pop()
                }

//                println(">>>>$it>" + r.reversed())

                lastNote = if (r.reversed()[0] in notePattenChars) {
                    // println(r.reversed())
                    // println(">>>>$it>" + r.reversed())
                    val note = toNote(r.reversed(), defaultPitch)
                    list.add(note)
                    note
                } else {

                    val note = convertPrevious(r.reversed(), lastNote!!)
                    list.add(note)
                    note
                }

                stack.push(char)
            }
        }
    }

    return list.joinToString(" ")
}

private fun convertPrevious(str: String, last: Note): Note {

//    var plus_count = 0
//    var minus_count = 0

    val nc = last.clone()

    str.forEach { char ->

        when (char) {
            '^' -> nc.up()
            'v' -> nc.down()
            '~' -> {}
            'm' -> nc.up(2)
            'w' -> nc.down(2)
            'n' -> nc.up(3)
            'u' -> nc.down(3)
            'i' -> nc.up(4)
            '!' -> nc.down(4)
            'p' -> nc.up(5)
            'q' -> nc.down(5)
            's' -> nc.up(6)
            'z' -> nc.down(6)
            '+' -> nc.longer()
            '-' -> nc.shorter()
            '.' -> nc.dot()
            ':' -> nc.intv0()
        }
    }
    return nc
}

private fun toNote(str: String, defaultPitch: Int = 4): Note {

    val note = Note("")

    str.forEach { char ->

        when (char) {
            in 'a'..'g' -> {
                note.root_note = char.uppercase(Locale.getDefault())
                note.num = defaultPitch
            }
            in 'A'..'G' -> {
                note.root_note = char.uppercase(Locale.getDefault())
                note.num = defaultPitch + 1 // 5
            }
            '$' -> note.bos = "b"
            '#' -> note.bos = "#"
            in '0'..'9' -> note.num = char.code - 48
            '+' -> note.longer()
            '-' -> note.shorter()
            '.' -> note.dot()
            ':' -> note.intv0()
        }
    }
    return note
}

private class Note(
    var root_note: String?,
    var num: Int = 4,
    var duration: Double = 4.0,
    var bos: String = "",
    var interval: String = "."
) {

    fun shorter() {
        this.duration = this.duration.div(2)
//        this.duration= this.duration.times(2)
    }

    fun longer() {
        this.duration = this.duration.times(2)
//        this.duration = this.duration.div(2)
    }

    fun intv(interval: String = ".") {
        this.interval = interval
    }

    fun intv0() {
        this.interval = "0"
//        this.duration = 0.0
    }

    fun dot() {
        this.duration = this.duration.times(1.5)
    }

    override fun toString(): String {
        return "$bos$root_note[$num,$duration]"
    }

    fun clone(): Note {
        return Note(root_note, num, duration, bos)
    }

    fun up(c: Int = 1): Note {
        for (i in 0 until c)
            when (root_note) {
                "C" -> root_note = "D"
                "D" -> root_note = "E"
                "E" -> root_note = "F"
                "F" -> root_note = "G"
                "G" -> root_note = "A"
                "A" -> root_note = "B"
                "B" -> {
                    root_note = "C"
                    this.num++
                }
            }
        return this
    }

    fun down(c: Int = 1): Note {
        for (i in 0 until c)
            when (root_note) {
                "C" -> {
                    root_note = "B"
                    this.num--
                }
                "B" -> root_note = "A"
                "A" -> root_note = "G"
                "G" -> root_note = "F"
                "F" -> root_note = "E"
                "E" -> root_note = "D"
                "D" -> root_note = "C"
            }
        return this
    }
}