package bot.music.whiter

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import whiter.music.mider.dsl.play
import whiter.music.mider.practise.absolutepitch.practise1
import java.net.URL
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext


fun main(args: Array<String>) {
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

    val f = Regex("[c-gC-GaA]").find("1b1b5566#50O 343vsss;l")
    println(f?.value)


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