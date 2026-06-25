import AVFoundation
import MediaPlayer

final class IOSNativeAudioEngine: NativeAudioEngine {
    private let player = AVPlayer()
    private var currentPayload: PlaybackPayload?

    init() {
        configureAudioSession()
        configureRemoteCommands()
    }

    func play(_ payload: PlaybackPayload) {
        guard let url = URL(string: payload.url) else { return }
        currentPayload = payload
        var assetOptions: [String: Any] = [:]
        if !payload.headers.isEmpty {
            assetOptions["AVURLAssetHTTPHeaderFieldsKey"] = payload.headers
        }
        let asset = AVURLAsset(url: url, options: assetOptions)
        player.replaceCurrentItem(with: AVPlayerItem(asset: asset))
        updateNowPlaying(payload: payload)
        player.play()
    }

    func pause() {
        player.pause()
    }

    func resume() {
        player.play()
    }

    private func configureAudioSession() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            assertionFailure(error.localizedDescription)
        }
    }

    private func configureRemoteCommands() {
        let center = MPRemoteCommandCenter.shared()
        center.playCommand.addTarget { [weak self] _ in
            self?.resume()
            return .success
        }
        center.pauseCommand.addTarget { [weak self] _ in
            self?.pause()
            return .success
        }
    }

    private func updateNowPlaying(payload: PlaybackPayload) {
        MPNowPlayingInfoCenter.default().nowPlayingInfo = [
            MPMediaItemPropertyTitle: payload.title,
            MPMediaItemPropertyArtist: payload.artists,
            MPMediaItemPropertyAlbumTitle: payload.album,
        ]
    }
}
