package dev.connect0459.mbtdodgekmp.shared.guest

import dev.connect0459.mbtdodgekmp.shared.game.GameRules
import io.github.charlietap.chasm.embedding.exports
import io.github.charlietap.chasm.embedding.instance
import io.github.charlietap.chasm.embedding.invoke
import io.github.charlietap.chasm.embedding.memory.readInt
import io.github.charlietap.chasm.embedding.module
import io.github.charlietap.chasm.embedding.shapes.Memory
import io.github.charlietap.chasm.embedding.shapes.expect
import io.github.charlietap.chasm.embedding.store
import io.github.charlietap.chasm.runtime.value.NumberValue

internal class ChasmGameRules(
    wasmBytes: ByteArray,
) : GameRules {
    private val guestService: GuestService = GuestServiceImpl(wasmBytes)

    // GuestServiceImpl encapsulates its own store/instance privately, so
    // spawnBlock's heap-boxed tuple return (which Chasm's codegen exposes
    // only as a bare pointer) needs its own low-level module/store/instance
    // to decode -- same reasoning as GuestMemoryTest.
    private val spawnBlockModule = module(wasmBytes).expect("Failed to decode guest.wasm")
    private val spawnBlockStore = store()
    private val spawnBlockInstance =
        instance(spawnBlockStore, spawnBlockModule, emptyList()).expect("Failed to instantiate guest.wasm")
    private val spawnBlockMemory = exports(spawnBlockInstance).first { it.name == "memory" }.value as Memory

    override fun movePlayer(
        x: Int,
        dx: Int,
        minX: Int,
        maxX: Int,
    ): Int = guestService.movePlayer(x, dx, minX, maxX)

    override fun fallStep(
        y: Int,
        speed: Int,
    ): Int = guestService.fallStep(y, speed)

    override fun isCollision(
        playerX: Int,
        playerWidth: Int,
        blockX: Int,
        blockSize: Int,
        playerY: Int,
        blockY: Int,
        hitMargin: Int,
    ): Boolean = guestService.isCollision(playerX, playerWidth, blockX, blockSize, playerY, blockY, hitMargin) == 1

    override fun isOffScreen(
        blockY: Int,
        screenHeight: Int,
    ): Boolean = guestService.isOffScreen(blockY, screenHeight) == 1

    override fun shouldSpawn(
        tickCount: Int,
        interval: Int,
    ): Boolean = guestService.shouldSpawn(tickCount, interval) == 1

    override fun incrementScore(score: Int): Int = guestService.incrementScore(score)

    override fun spawnBlock(
        seed: Int,
        minX: Int,
        maxX: Int,
    ): Pair<Int, Int> {
        val result =
            invoke(
                spawnBlockStore,
                spawnBlockInstance,
                "spawn_block",
                listOf(NumberValue.I32(seed), NumberValue.I32(minX), NumberValue.I32(maxX)),
            ).expect("Failed to invoke spawn_block")

        val pointer = (result.single() as NumberValue.I32).value
        val x = readInt(spawnBlockStore, spawnBlockMemory, pointer).expect("Failed to read x")
        val nextSeed = readInt(spawnBlockStore, spawnBlockMemory, pointer + 4).expect("Failed to read next_seed")
        return x to nextSeed
    }
}
