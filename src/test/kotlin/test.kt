package bot.music.whiter

import whiter.music.mider.dsl.MiderDSL
import whiter.music.mider.dsl.play

fun main(args: Array<String>) {
    MiderDSL.instrument.valueOf("piano")

//    play {
//        !toMiderNoteList("F+^B\$C6GFG CE\$")
//        debug()

//        toMiderNoteListv2("gE+E(def av[x,y] g@x@y)(ui=gCCCD)(ui)Oo~~|A1:A2:A3:A4 (a) ").forEach(::println)
//        toMiderNoteListv2("#3i*b", isStave = false).forEach(::println)

//    println(charCount("csacas?c?a", '?'))
//    val g = Regex("def\\s+([a-zA-Z_]\\w*)\\s*=\\s*[^>\\s][^>]*")
//    val r = g.matchEntire("def _ACCVA=2332saadefacdc def a=0")
//    println(r?.groups
//    val u = Regex("macro ([a-zA-Z_]\\w*)(\\s*,\\s*[a-zA-Z_]\\w*)*\\s*:\\s*[^>\\s][^>]*")
//    val i = u.matchEntire("macro hex,dex,mama,uiu : ni你傻逼哦")
//    val test = i?.groupValues
//    test?.forEach {
//        println(it)
//    }

    // C^^^^^^^

//    play {
//        defaultNoteDuration = 1
//        !"!A[5,0.25]"
//        debug()
//    }

    //F+^B'C6GFG CE' F DE'Db' C+ gb' CE' FE' F+ FE' FB' G+ + GB' C6C6B' C6 G+ GE' FGFE' C+ Cb' C+CE'FE'FG E'

//    CoroutineScope(EmptyCoroutineContext).launch {
//        val res = HttpClient().get<HttpResponse> {
//            url("https://c.runoob.com/front-end/854/")
//        }
//
//        println(res.readText())
//    }

//    val f = Regex("[c-gC-GaA]").find("1b1b5566#50O 343vsss;l")
//    println(f?.value)

//    val startRegex = Regex(">((g|f|\\d+b)(;([-+b#]?[A-G](min|maj|major|minor)?))?(;\\d)?(;vex|vex&au)?)>")
//    val cmdRegex = Regex("${startRegex.pattern}[\\S\\s]+")
//
//    val msg = """
//        >g>123
//        >g>abc
//        >g>5666
//        >f>89
//        >f;A>WW
//    """
//
//    val noteLists = msg.split(startRegex).toMutableList()
//    noteLists.removeFirst()
//    val configParts = startRegex.findAll(msg).map { it.value.replace(">", "") }.toList()
//
//    noteLists.forEachIndexed { index, content ->
//        val config = configParts[index]
//        println("'$content': $config, $index")
//    }

//    println("ffmpeg".execute(charset = "gbk"))

//    play {
//        defaultNoteDuration = 2
//        !"O*0.1 A[4,1] D[4,1] B[4,1]"
//    }


//    val file = File("debug-sandbox/data/bot.music.whiter.MidiProduce/tmp/mirai_audio_pcm_1653845068124.pcm")
    // val s = AudioSystem.getAudioInputStream(file)
//    var offset = 0
//    val bufferSize = file.length().toInt()
//    val audioData = ByteArray(bufferSize)
//
//    println(bufferSize)
//
//    val af = AudioFormat(44100f, 16, 2, true, false)
//    val info = DataLine.Info(SourceDataLine::class.java, af, bufferSize)
//    val sdl = AudioSystem.getLine(info) as SourceDataLine
//    sdl.open(af)
//    sdl.start()
//
//    while (offset < audioData.size) {
//        offset += sdl.write(audioData, offset, bufferSize)
//    }



//    play {
//        defaultNoteDuration = 1
//        bpm = 100
//        !toMiderStanderNoteString(toInMusicScoreList("""
//             a d.d- e+v g+ #f++ d.d- e+v a+ v+ d.d- D+b+g+ #f+ e+ C.C- b+ g+^ v+
//             (repeat 3:aaa)
//
//             (ew)
//        """.trimIndent()))
//
//        debug()
//    }



//    toInMusicScoreList("""
//        D!!!
//    """.trimIndent()).forEach(::println)
    //macro ([a-zA-Z_]\w*)(\s*,\s*([a-zA-Z_]\w*))*\s*:\s*[^>\s][^>]*
//    println(Regex("([a-zA-Z_]\\w*)").pattern)
//    println(macro("1234(def a:{233})12(=  a  )22(android) (if)if (修个小(bug)好难(急着要)不行！()就是)"))
//        note().code
//    }
}