package org.feeluown.mobile

import android.os.Process
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MAX_DEBUG_LOG_LINES = 200

class AndroidDebugLogRepository(
    override val isAvailable: Boolean,
) : DebugLogRepository {
    override suspend fun logLines(): List<String> {
        if (!isAvailable) return emptyList()
        return withContext(Dispatchers.IO) {
            val process = Runtime.getRuntime().exec(
                arrayOf(
                    "logcat",
                    "-d",
                    "-v",
                    "time",
                    "--pid=${Process.myPid()}",
                    "*:I",
                ),
            )
            try {
                val output = process.inputStream.bufferedReader().use { it.readText() }
                val error = process.errorStream.bufferedReader().use { it.readText() }
                val exitCode = process.waitFor()
                if (exitCode != 0) {
                    throw IllegalStateException(error.ifBlank { "logcat failed: $exitCode" })
                }
                output.lines()
                    .map { it.trimEnd() }
                    .filter { it.isNotBlank() }
                    .takeLast(MAX_DEBUG_LOG_LINES)
            } finally {
                process.destroy()
            }
        }
    }
}
