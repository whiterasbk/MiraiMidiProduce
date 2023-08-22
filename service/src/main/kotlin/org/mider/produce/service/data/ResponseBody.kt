package org.mider.produce.service.data

import kotlinx.serialization.Serializable

@Serializable
data class ResponseBody(
    val stateCode: Int,
    val state: String,
    val message: String,
    val type: String? = null,
    val link: List<Map<String, String>>? = null
)
