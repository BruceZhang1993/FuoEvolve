import SwiftUI

@main
struct FuoEvolveApp: App {
    @StateObject private var model = PlayerViewModel(
        core: PythonCoreBridge(),
        audio: IOSNativeAudioEngine()
    )

    var body: some Scene {
        WindowGroup {
            ContentView(model: model)
        }
    }
}
