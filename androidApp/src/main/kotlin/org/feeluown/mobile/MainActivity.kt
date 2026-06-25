package org.feeluown.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val scope = rememberCoroutineScope()
            val controller = remember {
                FuoPlayerController(
                    core = AndroidFuoCoreBridge(applicationContext),
                    audioEngine = AndroidNativeAudioEngine(applicationContext),
                    scope = scope,
                )
            }
            AppRoot(controller)
        }
    }
}
