import Shared
import SwiftUI

private let config = GameConfig(
  screenWidth: 300,
  screenHeight: 500,
  playerY: 460,
  playerWidth: 50,
  blockSize: 30,
  fallSpeed: 8,
  spawnInterval: 20,
  hitMargin: 8
)

private func newGameState() -> GameState {
  GameState(
    playerX: (config.screenWidth - config.playerWidth) / 2,
    blocks: [],
    score: 0,
    rngSeed: Int32(Date().timeIntervalSince1970),
    tickCount: 0,
    isGameOver: false
  )
}

private let moveStep: Int32 = 24

struct ContentView: View {
  private let rules = GuestServiceFactoryKt.createGameRules()
  private let timer = Timer.publish(every: 1.0 / 30.0, on: .main, in: .common).autoconnect()

  @State private var state = newGameState()
  @State private var pendingDx: Int32 = 0

  var body: some View {
    VStack(spacing: 16) {
      Text("Score: \(state.score)")
        .font(.title2)
        .monospacedDigit()

      GameCanvas(state: state, config: config)
        .frame(width: CGFloat(config.screenWidth), height: CGFloat(config.screenHeight))
        .background(Color.black)
        .clipShape(RoundedRectangle(cornerRadius: 12))

      if state.isGameOver {
        Button("Restart") {
          state = newGameState()
        }
        .buttonStyle(.borderedProminent)
      } else {
        HStack(spacing: 40) {
          Button {
            pendingDx = -moveStep
          } label: {
            Image(systemName: "arrow.left.circle.fill").font(.system(size: 44))
          }

          Button {
            pendingDx = moveStep
          } label: {
            Image(systemName: "arrow.right.circle.fill").font(.system(size: 44))
          }
        }
      }
    }
    .padding()
    .onReceive(timer) { _ in
      guard !state.isGameOver else { return }
      state = GameEngine.shared.tick(state: state, config: config, playerDx: pendingDx, rules: rules)
      pendingDx = 0
    }
  }
}

private struct GameCanvas: View {
  let state: GameState
  let config: GameConfig

  var body: some View {
    Canvas { context, size in
      let scaleX = size.width / CGFloat(config.screenWidth)
      let scaleY = size.height / CGFloat(config.screenHeight)

      let playerRect = CGRect(
        x: CGFloat(state.playerX) * scaleX,
        y: CGFloat(config.playerY) * scaleY,
        width: CGFloat(config.playerWidth) * scaleX,
        height: CGFloat(config.blockSize) * scaleY
      )
      context.fill(Path(playerRect), with: .color(.green))

      for block in state.blocks {
        let blockRect = CGRect(
          x: CGFloat(block.x) * scaleX,
          y: CGFloat(block.y) * scaleY,
          width: CGFloat(config.blockSize) * scaleX,
          height: CGFloat(config.blockSize) * scaleY
        )
        context.fill(Path(blockRect), with: .color(.red))
      }

      if state.isGameOver {
        context.fill(Path(CGRect(origin: .zero, size: size)), with: .color(.black.opacity(0.5)))
      }
    }
  }
}
