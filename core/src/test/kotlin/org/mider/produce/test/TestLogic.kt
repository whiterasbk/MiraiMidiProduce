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
>g;2x;bE;sing>f↓[明]f↓[明]d[很] e[想]-d.[吃]c[鸡]+a↓[却]b↓[又]c[忍]d[不]e[住]-d.[怀]c[疑]+o-c[在]+f[你]f-[的]e.[心]d[里]+d[我]c[是]g[否] g[就]e[是]-f.[唯]a[一]+b[爱]+a[就]g[是]f[请]f[我]d[吃]e[肯]-f.[德]g++[基]
        """.trimIndent()
        val generate = cfg.generate(code)
        val stream = generate.second.first().first
        println(System.getProperty("user.dir"))
        File(workDir, "g.mp3").writeBytes(stream.readAllBytes())
    }
}