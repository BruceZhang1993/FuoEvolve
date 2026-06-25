package org.feeluown.mobile

data class FuoTrack(
    val id: String,
    val title: String,
    val artists: String,
    val album: String,
    val source: String,
)

data class PlaybackPayload(
    val url: String,
    val title: String,
    val artists: String,
    val album: String,
    val source: String,
    val headers: Map<String, String> = emptyMap(),
    val coverUrl: String? = null,
)

enum class PlayerStatus {
    Idle,
    Loading,
    Playing,
    Paused,
    Error,
}

interface FuoCoreBridge {
    suspend fun initialize()
    suspend fun search(keyword: String): List<FuoTrack>
    suspend fun play(trackId: String): PlaybackPayload
    suspend fun next(): PlaybackPayload?
    suspend fun previous(): PlaybackPayload?
}

interface NativeAudioEngine {
    fun play(payload: PlaybackPayload)
    fun pause()
    fun resume()
    fun stop()
}
