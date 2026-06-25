import Foundation

struct FuoTrack: Identifiable, Decodable {
    let id: String
    let title: String
    let artists: String
    let album: String
    let source: String
}

struct PlaybackPayload: Decodable {
    let url: String
    let title: String
    let artists: String
    let album: String
    let source: String
    let headers: [String: String]
    let coverUrl: String?

    enum CodingKeys: String, CodingKey {
        case url
        case title
        case artists
        case album
        case source
        case headers
        case coverUrl = "cover_url"
    }
}

protocol FuoCoreBridge {
    func initialize() async throws
    func search(keyword: String) async throws -> [FuoTrack]
    func play(trackId: String) async throws -> PlaybackPayload
    func next() async throws -> PlaybackPayload?
    func previous() async throws -> PlaybackPayload?
}

protocol NativeAudioEngine {
    func play(_ payload: PlaybackPayload)
    func pause()
    func resume()
}

@MainActor
final class PlayerViewModel: ObservableObject {
    @Published var query = ""
    @Published var tracks: [FuoTrack] = []
    @Published var message = "网易云音乐"
    @Published var currentTitle = "未播放"
    @Published var isPlaying = false

    private let core: FuoCoreBridge
    private let audio: NativeAudioEngine

    init(core: FuoCoreBridge, audio: NativeAudioEngine) {
        self.core = core
        self.audio = audio
    }

    func initialize() async {
        do {
            try await core.initialize()
        } catch {
            message = error.localizedDescription
        }
    }

    func search() async {
        let keyword = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !keyword.isEmpty else {
            message = "请输入关键词"
            return
        }
        do {
            tracks = try await core.search(keyword: keyword)
            message = tracks.isEmpty ? "没有搜索结果" : "搜索到 \(tracks.count) 首"
        } catch {
            message = error.localizedDescription
        }
    }

    func play(_ track: FuoTrack) async {
        do {
            playPayload(try await core.play(trackId: track.id))
        } catch {
            message = error.localizedDescription
        }
    }

    func next() async {
        do {
            if let payload = try await core.next() {
                playPayload(payload)
            }
        } catch {
            message = error.localizedDescription
        }
    }

    func previous() async {
        do {
            if let payload = try await core.previous() {
                playPayload(payload)
            }
        } catch {
            message = error.localizedDescription
        }
    }

    func toggle() {
        if isPlaying {
            audio.pause()
            isPlaying = false
        } else {
            audio.resume()
            isPlaying = true
        }
    }

    private func playPayload(_ payload: PlaybackPayload) {
        audio.play(payload)
        currentTitle = "\(payload.title) - \(payload.artists)"
        isPlaying = true
    }
}
