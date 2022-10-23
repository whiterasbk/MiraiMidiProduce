package org.mider.produce.service.data

import kotlinx.serialization.Serializable
import org.mider.produce.core.Configuration

@Serializable
data class ServiceParameter(
    val midercode: String,
    var sinsySynAlpha: Float = 0.55f,
    var sinsyF0shift: Int = 0,
    var sinsyVibpower: Int = 1,
    var sinsyLink: String = "http://sinsy.sp.nitech.ac.jp",
    var recursionLimit: Int = 50,
    var silkBitsRate: Int = 24000,
    var cache: Boolean = false,
    var formatMode: String = "internal->java-lame",
    var macroUseStrictMode: Boolean = true,
    var isBlankReplaceWith0: Boolean = false,
    var quality: Int = 64,
) {
    fun copy(coreCfg: Configuration) {
        coreCfg.sinsySynAlpha = sinsySynAlpha
        coreCfg.sinsyF0shift = sinsyF0shift
        coreCfg.sinsyVibpower = sinsyVibpower
        coreCfg.sinsyLink = sinsyLink
        coreCfg.recursionLimit = recursionLimit
        coreCfg.silkBitsRate = silkBitsRate
        coreCfg.cache = cache
        coreCfg.formatMode = formatMode
        coreCfg.macroUseStrictMode = macroUseStrictMode
        coreCfg.isBlankReplaceWith0 = isBlankReplaceWith0
        coreCfg.quality = quality
    }
}
