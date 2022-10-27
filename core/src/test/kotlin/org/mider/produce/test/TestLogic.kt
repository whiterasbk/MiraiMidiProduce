package org.mider.produce.test

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mider.produce.core.Configuration
import org.mider.produce.core.generate
import org.mider.produce.core.utils.toPinyin
import whiter.music.mider.xml.LyricInception
import java.io.File

class TestLogic {

    val workDir = File("src/test/resources")
    val cfg = Configuration(workDir)

    @Test
    fun `test core logic`(): Unit = runBlocking {

        LyricInception.replace = { it.toPinyin() }

        val code = """

>g;sing:cn>
    5[起]1i.[来]7-[饥]2i-[寒]1i-[交]5-[迫]3-[滴]6+
    [奴]4[隶]0-5-[起]2i.[来]1i-[全]7-[世]6-[界]5-
    [受]4-[苦滴]3+[人]5[满]1i.[枪]7-[滴]2i-[热]1i-
    [血]5-[已]3-[经]6+[沸]4-[腾]6-[要]2i-[为]1i-
    7[真]2i[理而]4i[斗]71i+[争]1i-O-3i-[l日]2i-[世]7+[界]6-[打]
    7-[个]1i-[落]6-[花]7+[流]5-[水]5-[奴]4#-[隶]5-[们]6.[起]6-
    [来]2.[起]1i-7+[来]7-O-2i[不]2i.[要]7-[说]5-[我]5-[们]4#-[—]
    5-[无]3i+[所有]1i-[我]6-[们]7-[要]1i-[做]7[天]2i[下]1i[的]6[主]
    5+[人]5-O-3i.-[这]2i--[是]1i+[最]5.[后]3-[的]6+[斗]4-[争]0-2i.-
    [团]1i--[结]7+[起来]6[到]5[明]51\+[天]5\-O-5[英]3i+[特]2i[纳]5[雄]1i[耐]
    07.[尔]7-[就]6.[—]5#-[定]6[要]2i[实]2\i+[现]2\i-0-3i-
    [这]2i-[是]1i+[最]5.[后]3-[的]6+[斗]4-[争]0-2.-[团]1--
    [结]7+[起来]6[到]5[明]3i+[天]3i[英]5i+[特]4i[纳]3i[雄维]
    2\i.[耐]3\i-4i[尔]0-4i-[就]3i.[—]3i-[定]2i.[要]2i-[实]1i+[现]

 """.trimIndent()
        val generate = cfg.generate(code)
        val stream = generate.second.first().first
        println(System.getProperty("user.dir"))
        File(workDir, "g.mp3").writeBytes(stream.readAllBytes())
    }
}