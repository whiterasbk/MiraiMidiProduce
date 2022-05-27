package bot.music.whiter

import net.mamoe.mirai.console.util.cast
import whiter.music.mider.dsl.MiderDSL
import java.time.Duration
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow

fun toMiderNoteListv2(seq: String, pitch: Int = 4): List<InMusicScore> {
    val list = mutableListOf<InMusicScore>()
    val doAfter = mutableListOf<()->Unit>()
    seq.forEachIndexed { index, char ->

        when (char) {

            '(' -> {
                if (seq[index + 1] == ':') {
                    return@forEachIndexed
                }
            }

            in 'a'..'g' -> {
                list += _Note(char, pitch = pitch)
            }

            in 'A'..'G' -> {
                list += _Note(char, pitch = pitch + 1)
            }

            'O' -> {
                doAfter.clear()
                list += Rest().let { it.duration.double; it }
            }

            'o' -> {
                doAfter.clear()
                list += Rest()
            }

            '~' -> {
                list += list.last().clone()
            }

            '^' -> {
                if (list.last() is _Note) {
                    list += list.last().clone().cast<_Note>().upperNoteName()
                }
            }

            'v' -> {
                if (list.last() is _Note) {
                    list += list.last().clone().cast<_Note>().lowerNoteName()
                }
            }

            in '0'..'9' -> {
                if (list.last() is IHasPitch)
                    list.last().cast<IHasPitch>().pitch = char.code - 48
                else if (list.last() is Chord)
                    list.last().cast<Chord>().notes.last().pitch = char.code - 48
            }

            '#' -> {
                doAfter += {
                    (list.last() as? IHasPitch)?.sharp()
                }
            }

            '&' -> {
                doAfter += {
                    if (list.last() is _Note)
                        list.last().cast<_Note>().isNature = true
                }
            }

            '$' -> {
                doAfter += {
                    if (list.last() is _Note)
                        list.last().cast<_Note>().flap()
                }
            }

            '\'' -> { if (list.last() is _Note) list.last().cast<_Note>().flap() }

            ':' -> {
                val chord: Chord = if (list.last() is _Note) {
                    val c = Chord(list.removeLast().cast())
                    list += c
                    c
                } else if (list.last() is Chord) {
                    list.last().cast()
                } else throw Exception("build chord failed: unsupported type: ${list.last()}")

                doAfter += {
                    chord += list.removeLast().cast()
                }
            }

            '+' -> {
                if (list.last() is _Note || list.last() is Rest)
                    list.last().duration.double
            }

            '-' -> {
                if (list.last() is _Note || list.last() is Rest)
                    list.last().duration.halve
            }

            '.' -> {
                if (list.last() is _Note || list.last() is Rest)
                    list.last().duration.point
            }
        }

        when(char) {
            in "abcdefgABCDEFG~^vmwnui!pqsz" -> {
                doAfter.asReversed().forEach { it() }
                doAfter.clear()
            }
        }

    }

    return list
}


interface InMusicScore: Cloneable {
    val duration: DurationDescribe
    public override fun clone(): InMusicScore

    class DurationDescribe (
        var bar: Int = 0, // 符杆数, 默认为 0 也就是 四分音符
        var dot: Int = 0 // 附点数
    ): Cloneable {

        val point: DurationDescribe get() {
            dot ++
            return this
        }

        val halve: DurationDescribe get() {
            bar --
            return this
        }

        val double: DurationDescribe get() {
            bar ++
            return this
        }

        public override fun clone(): DurationDescribe {
            return DurationDescribe(bar, dot)
        }

        override fun toString(): String {
            return (.25 * 2.0.pow(bar) * 1.5.pow(dot)).toString()
        }
    }
}

private interface IHasPitch {
    var pitch: Int
    var code: Int

    operator fun plusAssign(addPitch: Int) {
        code += addPitch * 12
    }

    operator fun minusAssign(addPitch: Int) {
        code -= addPitch * 12
    }

    fun sharp(times: Int = 1) {
        code += times
    }

    fun flap(times: Int = 1) {
        code -= times
    }

}

private class Chord(vararg firstNotes: _Note) : InMusicScore {

    init {
        if (firstNotes.isEmpty()) throw Exception("a chord needs notes to buildup")
    }

    val notes = firstNotes.toMutableList()
    val rootNote get() = notes[0]
    val secondNote get() = notes[1]
    val thirdNote get() = notes[2]
    val forthNote get() = notes[3]

    override val duration: InMusicScore.DurationDescribe = rootNote.duration

    override fun clone(): Chord {
        val cloneNotes = mutableListOf<_Note>()
        notes.forEach {
            cloneNotes += it.clone()
        }

        return Chord(*cloneNotes.toTypedArray())
    }

    operator fun plusAssign(note: _Note) {
        notes += note
    }

    override fun toString(): String {
        return "Chord: " + notes.joinToString(" ")
    }
}

private class Rest(override val duration: InMusicScore.DurationDescribe = InMusicScore.DurationDescribe()) : InMusicScore {
    override fun clone(): Rest {
        return Rest(duration.clone())
    }

    override fun toString(): String = "[Rest|$duration]"
}

private class _Note(
    override var code: Int,
    override val duration: InMusicScore.DurationDescribe = InMusicScore.DurationDescribe(),
    val velocity: Int = 100,
    var isNature: Boolean = false // 是否添加了还原符号
) : InMusicScore, IHasPitch {

    constructor(name: String, pitch: Int = 4, duration: InMusicScore.DurationDescribe = InMusicScore.DurationDescribe(), velocity: Int = 100)
            : this(noteBaseOffset (name) + (pitch + 1) * 12, duration, velocity)
    constructor(name: Char, pitch: Int = 4, duration: InMusicScore.DurationDescribe = InMusicScore.DurationDescribe(), velocity: Int = 100)
            : this(name.uppercase(), pitch, duration, velocity)

    override var pitch: Int = 0
     get() {
        return code / 12 - 1
    } set(value) {
        code = code % 12 + value * 12
        field = value
    }

    fun upperNoteName(times: Int = 1): _Note {
        return this
    }

    fun lowerNoteName(times: Int = 1): _Note {
        return this
    }

    override fun clone(): _Note {
        return _Note(code, duration.clone())
    }

    override fun toString(): String = "[$code>${noteNameFromCode(code)}$pitch|$duration|$velocity]"
}

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