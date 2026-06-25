package org.feeluown.mobile

import android.content.Context
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AndroidFuoCoreBridge(
    private val context: Context,
) : FuoCoreBridge {
    private var bridge: PyObject? = null

    override suspend fun initialize() {
        withContext(Dispatchers.IO) {
            if (bridge != null) return@withContext
            val providers = context.assets.open("providers.json")
                .bufferedReader()
                .use { it.readText() }
            bridge = Python.getInstance()
                .getModule("fuo_mobile.bridge")
                .callAttr("create_bridge", providers)
        }
    }

    override suspend fun search(keyword: String): List<FuoTrack> {
        initialize()
        return withContext(Dispatchers.IO) {
            val raw = requireNotNull(bridge).callAttr("search", keyword).toString()
            val array = JSONObject(raw).getJSONArray("tracks")
            List(array.length()) { index ->
                array.getJSONObject(index).toTrack()
            }
        }
    }

    override suspend fun play(trackId: String): PlaybackPayload {
        initialize()
        return withContext(Dispatchers.IO) {
            val raw = requireNotNull(bridge).callAttr("play", trackId).toString()
            JSONObject(raw).toPayload()
        }
    }

    override suspend fun next(): PlaybackPayload? {
        initialize()
        return withContext(Dispatchers.IO) {
            val raw = requireNotNull(bridge).callAttr("next").toString()
            if (raw == "null") null else JSONObject(raw).toPayload()
        }
    }

    override suspend fun previous(): PlaybackPayload? {
        initialize()
        return withContext(Dispatchers.IO) {
            val raw = requireNotNull(bridge).callAttr("previous").toString()
            if (raw == "null") null else JSONObject(raw).toPayload()
        }
    }

    private fun JSONObject.toTrack(): FuoTrack = FuoTrack(
        id = getString("id"),
        title = optString("title"),
        artists = optString("artists"),
        album = optString("album"),
        source = optString("source"),
    )

    private fun JSONObject.toPayload(): PlaybackPayload = PlaybackPayload(
        url = getString("url"),
        title = optString("title"),
        artists = optString("artists"),
        album = optString("album"),
        source = optString("source"),
        headers = optJSONObject("headers").toStringMap(),
        coverUrl = optString("cover_url").takeIf { it.isNotBlank() },
    )

    private fun JSONObject?.toStringMap(): Map<String, String> {
        if (this == null) return emptyMap()
        val keys = keys()
        val result = linkedMapOf<String, String>()
        while (keys.hasNext()) {
            val key = keys.next()
            result[key] = optString(key)
        }
        return result
    }
}
