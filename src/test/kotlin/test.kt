package bot.music.whiter

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import whiter.music.mider.code.produceCore
import whiter.music.mider.dsl.Dsl2MusicXml
import whiter.music.mider.dsl.MiderDSL
import whiter.music.mider.dsl.playDslInstance
import java.io.File
import java.io.InputStream


suspend fun main(args: Array<String>) {

//        val k =
//        val kj = k.find("20220814162448_9636.wav\">wav</a>, <a target=\"_top\" href=\"./")
//        println(kj?.value)

//    val k = Regex("[\\w'\"\\-+#@:,.\\[\\]()]+\\.wav")
//        .find("20220814200451_7248.wav\">wav</a>, <a target=\"_top\" href=\"./")?.value
//    println(k)


    playMiderCodeFile("C:\\Users\\whiter\\Desktop\\what.midercode")




//    val singer = selectSinger("" to "")
//    val sinsyCfg = SinsyConfig(singer.second, singer.first)
//    val after = sinsy("D:\\ProjectFiles\\idea\\mider\\src\\test\\resources\\3format.xml", sinsyCfg)
//    File("atmp.wav").writeBytes(after.readAllBytes())

//        val client = HttpClient(OkHttp)
//        val k = client.get<InputStream>("http://sinsy.sp.nitech.ac.jp/temp/20220814162448_9636.wav")
//        File("tmp.wav").writeBytes(k.readAllBytes())

//        sinsy("")
//
//        val k = produceCore("""
//            >240b;Bmin;i=musicbox>faaabDba | b-D-~~Db-a-a++ | F~~~ GFED | E-~~~EE-F-E++ | FFF-F F-GFED | bDD b-a- a++ | FFF-F F-GFED | a-~~~EC D++-+
//            >240b;Bmin;i=oboe>faaabDba | b-D-~~Db-a-a++ | F~~~ GFED | E-~~~EE-F-E++ | FFF-F F-GFED | bDD b-a- a++ | FFF-F F-GFED | a-~~~EC D++-+
//        """.trimIndent())
//
//        playDslInstance(miderDSL = k.miderDSL)


//       play {
//              O*15; A+D
//              debug()
//       }
//    MiderDSL.instrument.valueOf("piano")
//       toInMusicScoreList("""
//            (repeat 16:1↑2↑5↑1↑2↑5↑1↑2↑5↑1↑2↑5↑)(repeat 4:671↑671↑45645651↑3↑51↑3↑572↑572↑)
//            (repeat 4:3++0++0++4++0++0++5++0++0++6++005++00)(repeat 4:6006004004001↑001↑00500500)
//            0++033022011000000++000+00333022013003052001000++033042011000000++000+02222032011++000++000++033022011000000++000+00333022013003052001000++033042011000000++000+02222032011+0000++0055555055043021001023022005555055043023000212000001023040032011031020000001023040034050043022032011
//
//       """.trimIndent(), useMacro = true, isStave = false).forEach(::println)


//       println(macro("""
//             (# aa)
//       """))

//       println("4t234".nextOnlyInt(0,3))

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