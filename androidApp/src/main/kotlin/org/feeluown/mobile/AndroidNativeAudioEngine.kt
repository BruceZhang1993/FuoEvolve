package org.feeluown.mobile

import android.content.Context
import org.json.JSONObject

class AndroidNativeAudioEngine(
    private val context: Context,
) : NativeAudioEngine {
    override fun play(payload: PlaybackPayload) {
        FuoPlaybackService.play(context, payload.toJson())
    }

    override fun pause() {
        FuoPlaybackService.pause(context)
    }

    override fun resume() {
        FuoPlaybackService.resume(context)
    }

    override fun stop() {
        FuoPlaybackService.stop(context)
    }

    private fun PlaybackPayload.toJson(): String {
        val headersJson = JSONObject()
        headers.forEach { (key, value) -> headersJson.put(key, value) }
        return JSONObject()
            .put("url", url)
            .put("title", title)
            .put("artists", artists)
            .put("album", album)
            .put("source", source)
            .put("headers", headersJson)
            .put("cover_url", coverUrl ?: "")
            .toString()
    }
}
