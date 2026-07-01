package org.feeluown.mobile

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidLocalMusicRepository(
    private val context: Context,
) : LocalMusicRepository {
    private var cachedTracks: List<MusicTrack> = emptyList()
    private var scanSettings = LocalMusicScanSettings()

    override suspend fun updateScanSettings(settings: LocalMusicScanSettings) {
        withContext(Dispatchers.IO) {
            scanSettings = settings
        }
    }

    override suspend fun directories(): List<LocalMusicDirectory> = withContext(Dispatchers.IO) {
        val counts = linkedMapOf<String, Pair<String, Int>>()
        queryAudioRows { row ->
            val directory = directoryInfo(row.relativePath)
            val current = counts[directory.id]
            counts[directory.id] = directory.name to ((current?.second ?: 0) + 1)
        }
        counts.map { (id, value) ->
            LocalMusicDirectory(
                id = id,
                name = value.first,
                trackCount = value.second,
            )
        }.sortedBy { it.name }
    }

    override suspend fun scan(): List<MusicTrack> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<MusicTrack>()
        val settings = scanSettings
        queryAudioRows { row ->
            val directory = directoryInfo(row.relativePath)
            if (directory.id in settings.excludedDirectoryIds) return@queryAudioRows
            val durationMs = row.durationMs.takeIf { it > 0 }
            if (settings.minDurationSeconds > 0 &&
                (durationMs == null || durationMs < settings.minDurationSeconds * 1000L)
            ) {
                return@queryAudioRows
            }
            val sourceType = if (row.relativePath.contains(FEELUOWN_FOLDER)) {
                TrackSourceType.Downloaded
            } else {
                TrackSourceType.LocalMediaStore
            }
            tracks += MusicTrack(
                id = if (sourceType == TrackSourceType.Downloaded) {
                    "downloaded:${row.uri}"
                } else {
                    "local:${row.uri}"
                },
                title = row.title,
                artists = row.artist,
                album = row.album,
                source = "local",
                sourceType = sourceType,
                coverUrl = localCoverUri(row.uri, row.albumId),
                durationMs = durationMs,
                localUri = row.uri.toString(),
            )
        }
        cachedTracks = tracks
        tracks
    }

    override suspend fun search(keyword: String): List<MusicTrack> {
        val tracks = cachedTracks.ifEmpty { scan() }
        val normalized = keyword.trim()
        if (normalized.isEmpty()) return emptyList()
        return tracks.filter {
            it.title.contains(normalized, ignoreCase = true) ||
                it.artists.contains(normalized, ignoreCase = true) ||
                it.album.contains(normalized, ignoreCase = true)
        }
    }

    private fun queryAudioRows(onRow: (AudioRow) -> Unit) {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Audio.Media.RELATIVE_PATH)
            }
        }.toTypedArray()
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        try {
            context.contentResolver.query(collection, projection, selection, null, sortOrder)
        } catch (_: SecurityException) {
            null
        }?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val relativePathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
            } else {
                -1
            }
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(collection, id)
                onRow(
                    AudioRow(
                        uri = uri,
                        title = cursor.getString(titleColumn).orEmpty(),
                        artist = cursor.getString(artistColumn).orEmpty(),
                        album = cursor.getString(albumColumn).orEmpty(),
                        albumId = cursor.getLong(albumIdColumn),
                        durationMs = cursor.getLong(durationColumn),
                        relativePath = if (relativePathColumn >= 0) {
                            cursor.getString(relativePathColumn).orEmpty()
                        } else {
                            ""
                        },
                    )
                )
            }
        }
    }

    private fun directoryInfo(relativePath: String): LocalMusicDirectory {
        val normalized = relativePath.trim('/').ifBlank { OTHER_DIRECTORY_ID }
        val name = if (normalized == OTHER_DIRECTORY_ID) "其他媒体库" else normalized
        return LocalMusicDirectory(
            id = normalized,
            name = name,
            trackCount = 0,
        )
    }

    private companion object {
        private const val FEELUOWN_FOLDER = "FeelUOwn"
        private const val OTHER_DIRECTORY_ID = "__other__"
    }
}

private data class AudioRow(
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val relativePath: String,
)

private fun albumArtUri(albumId: Long): String? {
    if (albumId <= 0) return null
    return Uri.parse("content://media/external/audio/albumart")
        .buildUpon()
        .appendPath(albumId.toString())
        .build()
        .toString()
}

private fun localCoverUri(audioUri: Uri, albumId: Long): String {
    return Uri.parse("fuo-cover://local")
        .buildUpon()
        .appendQueryParameter("audio", audioUri.toString())
        .appendQueryParameter("albumArt", albumArtUri(albumId).orEmpty())
        .build()
        .toString()
}
