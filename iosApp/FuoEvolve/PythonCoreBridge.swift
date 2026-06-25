import Foundation

final class PythonCoreBridge: FuoCoreBridge {
    func initialize() async throws {
        // Wire Python Apple Support here once Python.xcframework is added.
    }

    func search(keyword: String) async throws -> [FuoTrack] {
        throw BridgeError.pythonRuntimeNotLinked
    }

    func play(trackId: String) async throws -> PlaybackPayload {
        throw BridgeError.pythonRuntimeNotLinked
    }

    func next() async throws -> PlaybackPayload? {
        throw BridgeError.pythonRuntimeNotLinked
    }

    func previous() async throws -> PlaybackPayload? {
        throw BridgeError.pythonRuntimeNotLinked
    }
}

enum BridgeError: LocalizedError {
    case pythonRuntimeNotLinked

    var errorDescription: String? {
        "Python Apple Support is not linked yet"
    }
}
