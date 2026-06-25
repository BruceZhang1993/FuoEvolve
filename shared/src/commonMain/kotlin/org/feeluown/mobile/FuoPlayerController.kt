package org.feeluown.mobile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class FuoPlayerController(
    private val core: FuoCoreBridge,
    private val audioEngine: NativeAudioEngine,
    private val scope: CoroutineScope,
) {
    var query by mutableStateOf("")
        private set
    var tracks by mutableStateOf<List<FuoTrack>>(emptyList())
        private set
    var current by mutableStateOf<PlaybackPayload?>(null)
        private set
    var status by mutableStateOf(PlayerStatus.Idle)
        private set
    var message by mutableStateOf("网易云音乐")
        private set

    init {
        scope.launch {
            runCatching { core.initialize() }
                .onFailure { setError(it) }
        }
    }

    fun onQueryChange(value: String) {
        query = value
    }

    fun search() {
        val keyword = query.trim()
        if (keyword.isEmpty()) {
            message = "请输入关键词"
            return
        }
        scope.launch {
            status = PlayerStatus.Loading
            runCatching { core.search(keyword) }
                .onSuccess {
                    tracks = it
                    status = current?.let { PlayerStatus.Paused } ?: PlayerStatus.Idle
                    message = if (it.isEmpty()) "没有搜索结果" else "搜索到 ${it.size} 首"
                }
                .onFailure { setError(it) }
        }
    }

    fun play(track: FuoTrack) {
        scope.launch {
            status = PlayerStatus.Loading
            runCatching { core.play(track.id) }
                .onSuccess { playPayload(it) }
                .onFailure { setError(it) }
        }
    }

    fun toggle() {
        when (status) {
            PlayerStatus.Playing -> {
                audioEngine.pause()
                status = PlayerStatus.Paused
            }
            PlayerStatus.Paused, PlayerStatus.Idle -> {
                current?.let {
                    audioEngine.resume()
                    status = PlayerStatus.Playing
                }
            }
            else -> Unit
        }
    }

    fun next() {
        scope.launch {
            runCatching { core.next() }
                .onSuccess { payload -> payload?.let { playPayload(it) } }
                .onFailure { setError(it) }
        }
    }

    fun previous() {
        scope.launch {
            runCatching { core.previous() }
                .onSuccess { payload -> payload?.let { playPayload(it) } }
                .onFailure { setError(it) }
        }
    }

    private fun playPayload(payload: PlaybackPayload) {
        current = payload
        audioEngine.play(payload)
        status = PlayerStatus.Playing
        message = "${payload.title} - ${payload.artists}"
    }

    private fun setError(throwable: Throwable) {
        status = PlayerStatus.Error
        message = throwable.message ?: throwable::class.simpleName.orEmpty()
    }
}
