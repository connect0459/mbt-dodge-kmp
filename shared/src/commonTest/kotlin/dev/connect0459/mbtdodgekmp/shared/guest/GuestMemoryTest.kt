package dev.connect0459.mbtdodgekmp.shared.guest

import io.github.charlietap.chasm.embedding.exports
import io.github.charlietap.chasm.embedding.instance
import io.github.charlietap.chasm.embedding.invoke
import io.github.charlietap.chasm.embedding.memory.readInt
import io.github.charlietap.chasm.embedding.module
import io.github.charlietap.chasm.embedding.shapes.Memory
import io.github.charlietap.chasm.embedding.shapes.expect
import io.github.charlietap.chasm.embedding.store
import io.github.charlietap.chasm.runtime.value.NumberValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GuestMemoryTest {
    // GuestServiceImpl (the Chasm-generated wrapper) exposes spawnBlock as a
    // plain Int, since Chasm's codegen has no notion of a MoonBit-boxed
    // tuple -- it only sees a wasm function returning one i32. GuestServiceImpl
    // doesn't expose the store/instance that pointer is only valid within, so
    // decoding it requires Chasm's low-level embedding API directly.
    private fun callSpawnBlock(
        seed: Int,
        minX: Int,
        maxX: Int,
    ): Pair<Int, Int> {
        val module = module(GUEST_WASM_BYTES).expect("Failed to decode guest.wasm")
        val store = store()
        val instance = instance(store, module, emptyList()).expect("Failed to instantiate guest.wasm")

        val result =
            invoke(
                store,
                instance,
                "spawn_block",
                listOf(NumberValue.I32(seed), NumberValue.I32(minX), NumberValue.I32(maxX)),
            ).expect("Failed to invoke spawn_block")

        val pointer = (result.single() as NumberValue.I32).value
        val memory = exports(instance).first { it.name == "memory" }.value as Memory

        val x = readInt(store, memory, pointer).expect("Failed to read x")
        val nextSeed = readInt(store, memory, pointer + 4).expect("Failed to read next_seed")

        return x to nextSeed
    }

    @Test
    fun spawnBlockReturnsAPointerToAHeapAllocatedTupleInLinearMemory() {
        val (x, nextSeed) = callSpawnBlock(seed = 1, minX = 0, maxX = 9)

        assertTrue(x in 0..9)
        assertTrue(nextSeed != 1)
    }

    @Test
    fun spawnBlockIsDeterministicForAGivenSeed() {
        assertEquals(callSpawnBlock(42, 0, 100), callSpawnBlock(42, 0, 100))
    }
}
