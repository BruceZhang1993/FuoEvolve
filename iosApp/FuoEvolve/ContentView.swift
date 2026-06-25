import SwiftUI

struct ContentView: View {
    @ObservedObject var model: PlayerViewModel

    var body: some View {
        VStack(spacing: 12) {
            Text("FeelUOwn")
                .font(.largeTitle.weight(.semibold))
                .frame(maxWidth: .infinity, alignment: .leading)

            HStack {
                TextField("搜索网易云音乐", text: $model.query)
                    .textFieldStyle(.roundedBorder)
                Button("搜索") {
                    Task { await model.search() }
                }
            }

            Text(model.message)
                .font(.footnote)
                .frame(maxWidth: .infinity, alignment: .leading)
                .lineLimit(1)

            List(model.tracks) { track in
                Button {
                    Task { await model.play(track) }
                } label: {
                    VStack(alignment: .leading) {
                        Text(track.title)
                            .font(.headline)
                            .lineLimit(1)
                        Text([track.artists, track.album].filter { !$0.isEmpty }.joined(separator: " · "))
                            .font(.caption)
                            .foregroundStyle(.secondary)
                            .lineLimit(1)
                    }
                }
            }

            VStack(alignment: .leading, spacing: 8) {
                Text(model.currentTitle)
                    .font(.subheadline.weight(.medium))
                    .lineLimit(1)
                HStack {
                    Button("上一首") { Task { await model.previous() } }
                    Button(model.isPlaying ? "暂停" : "播放") { model.toggle() }
                    Button("下一首") { Task { await model.next() } }
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding()
        .task {
            await model.initialize()
        }
    }
}
