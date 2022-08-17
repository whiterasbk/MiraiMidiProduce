package bot.music.whiter


import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent

/**
 * @param which gameId
 */
suspend fun MessageEvent.gameStart(which: String) {
    when (which) {
        "relative-pitch-practice", "rpp" -> {
            if (this is GroupMessageEvent) {
                group.sendMessage("")
            }
        }
    }
}

class MutableTriple <out A, out B, out C> (
    var first: @UnsafeVariance A,
    var second: @UnsafeVariance B,
    var third: @UnsafeVariance C
) {
    override fun toString(): String = "($first, $second, $third)"
}

class DurativeMessageHandler {
    val actions = mutableSetOf<MutableTriple<Boolean, Long, (MutableSet<MutableTriple<Boolean, Long, (Any) -> Boolean>>) -> Boolean>>()



    fun handle() {
        actions.forEach {
            // it.first = it.third(actions)
        }
    }
}