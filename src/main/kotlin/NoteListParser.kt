package bot.music.whiter

import java.util.*

fun toMiderNoteList(str: String, defaultPitch: Int = 4): String {
    var list = mutableListOf<Note>()
    val stack = MStack<Char>()
    var cha = str + "a"
    var last_note:Note? = null

    val not_from_previous_regex = Regex("[^\\^vi~!mwnupqsz]")
    val note_patten_regex = Regex("[a-gA-G]")
    val from_previous_regex = Regex("[\\^vi~!mwnupqsz]")
    val note_modify_regex = Regex("[0-9$:.#+-]")

    cha.forEach {
        val char = it.toString()
        if (char.matches(not_from_previous_regex)) {


            if (char.matches(note_patten_regex)) {
                // println("iLLLg: $it")
                if (!stack.isEmpty) {
                    var r = ""
                    while (!stack.isEmpty) {
                        r += stack.pop()
                    }

                    // println("inaagg: ${r.reversed()}")

                    if (r.reversed()[0].toString().matches(from_previous_regex)) {
                        val nc = convertPrevious(r.reversed(), last_note!!)
                        last_note = nc
                        list.add(nc)
                    } else {
                        val n = toNote(r.reversed(), defaultPitch)
                        last_note = n
                        list.add(n)
                    }


                }

                stack.push(it)
            } else {

                if (char.matches(note_modify_regex)) {
                    stack.push(it)
                }
            }
        } else {

            // println(">>$it  $last_note")

            if (!stack.isEmpty) {
                var r = ""
                while (!stack.isEmpty) {
                    r += stack.pop()
                }

//                println(">>>>$it>" + r.reversed())

                if (r.reversed()[0].toString().matches(note_patten_regex)) {
                    // println(r.reversed())
                    // println(">>>>$it>" + r.reversed())
                    val note = toNote(r.reversed(), defaultPitch)
                    list.add(note)
                    last_note = note
                } else {

                    val note_ = convertPrevious(r.reversed(), last_note!!)
                    list.add(note_)
                    last_note = note_
                }

                stack.push(it)
            }
        }
    }

    return list.joinToString(" ")
}

private fun convertPrevious(str: String, last: Note):Note {

//    var plus_count = 0
//    var minus_count = 0

    val nc = last.clone()

    str.forEach {
        val char = it.toString()
        if (char.matches(Regex("[\\^]"))) {
            nc.up()
        } else if (char.matches(Regex("[v]"))) {
            nc.down()
        } else if (char.matches(Regex("[~]"))) {

        } else if (char.matches(Regex("[m]"))) {
            nc.up(2)
        } else if (char.matches(Regex("[w]"))) {
            nc.down(2)
        } else if (char.matches(Regex("[n]"))) {
            nc.up(3)
        } else if (char.matches(Regex("[u]"))) {
            nc.down(3)
        } else if (char.matches(Regex("[i]"))) {
            nc.up(4)
        } else if (char.matches(Regex("[!]"))) {
            nc.down(4)
        } else if (char.matches(Regex("[p]"))) {
            nc.up(5)
        } else if (char.matches(Regex("[q]"))) {
            nc.down(5)
        } else if (char.matches(Regex("[s]"))) {
            nc.up(6)
        } else if (char.matches(Regex("[z]"))) {
            nc.down(6)
        } else if (char.matches(Regex("[+]"))) {
            nc.longer()
        } else if (char.matches(Regex("[-]"))) {
            nc.shorter()
        } else if (char.matches(Regex("[.]"))) {
            nc.dot()
        } else if (char.matches(Regex("[:]"))) {
            nc.intv0()
        }
    }
    return nc
}

private fun toNote(str: String, defaultPitch: Int = 4) : Note {

    val note = Note("")

    str.forEach {
        val char = it.toString()
        if (char.matches(Regex("[a-g]"))) {
            note.root_note = char.uppercase(Locale.getDefault())
            note.num = defaultPitch
        } else if (char.matches(Regex("[A-G]"))) {
            note.root_note = char.uppercase(Locale.getDefault())
            note.num = defaultPitch + 1 // 5
        } else if (char.matches(Regex("[$#]"))) {
            when (it) {
                '$' -> note.bos = "b"
                '#' -> note.bos = "#"
            }
        } else if (char.matches(Regex("[0-9]"))) {
            note.num = char.toInt()
        } else if (char.matches(Regex("[+]"))) {
            note.longer()
        } else if (char.matches(Regex("[-]"))) {
            note.shorter()
        } else if (char.matches(Regex("[.]"))) {
            note.dot()
        } else if (char.matches(Regex("[:]"))) {
            note.intv0()
        }
    }
    return note
}

private class Note(var root_note: String?, var num: Int = 4, var duration: Double = 4.0, var bos: String = "", var interval: String = ".") {

    fun shorter() {
        this.duration= this.duration.div(2)
//        this.duration= this.duration.times(2)
    }

    fun longer() {
        this.duration = this.duration.times(2)
//        this.duration = this.duration.div(2)
    }

    fun intv(interval: String = "."){
        this.interval = interval
    }

    fun intv0(){
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

    fun up(c: Int = 1):Note {
        for (i in 0 until c)
            when(root_note) {
                "C" -> root_note = "D"
                "D" -> root_note = "E"
                "E" -> root_note = "F"
                "F" -> root_note = "G"
                "G" -> root_note = "A"
                "A" -> root_note = "B"
                "B" -> {
                    root_note = "C"
                    this.num ++
                }
            }
        return this
    }

    fun down(c: Int = 1): Note {
        for (i in 0 until c)
            when(root_note) {
                "C" -> {
                    root_note = "B"
                    this.num --
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

internal class MStack<T> {
    //实现栈的数组
    private var stack: Array<Any?>

    //数组大小
    private var size = 0

    //判断是否为空
    val isEmpty: Boolean
        get() = size == 0

    //返回栈顶元素
    fun peek(): T? {
        var t: T? = null
        if (size > 0) t = stack[size - 1] as T?
        return t
    }

    fun push(t: T) {
        expandCapacity(size + 1)
        stack[size] = t
        size++
    }

    //出栈
    fun pop(): T? {
        val t = peek()
        if (size > 0) {
            stack[size - 1] = null
            size--
        }
        return t
    }

    //扩大容量
    fun expandCapacity(size: Int) {
        var size = size
        val len = stack.size
        if (size > len) {
            size = size * 3 / 2 + 1 //每次扩大50%
            stack = Arrays.copyOf(stack, size)
        }
    }

    override fun toString(): String {
        var res = ""
        for(e in stack) {
            res += " $e"
        }
        return res
    }

    init {
        stack = arrayOfNulls(20) //初始容量为10
    }
}