package org.mider.produce

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Test
import org.mider.produce.service.data.ServiceParameter

class ApplicationTest {

    @Test
    fun `test data service parameter`() {
        val b = Json.decodeFromString<ServiceParameter>("""{}""")
        println(b)
    }
}