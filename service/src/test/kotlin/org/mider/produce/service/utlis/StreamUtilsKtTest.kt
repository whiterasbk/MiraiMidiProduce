package org.mider.produce.service.utlis

import org.junit.jupiter.api.Test

internal class StreamUtilsKtTest {

    @Test
    fun hash() {
        val hash = "123 ".hash()
        println(hash)
    }
}