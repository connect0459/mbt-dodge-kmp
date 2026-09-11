package dev.connect0459.mbtdodgekmp.shared.guest

import dev.connect0459.mbtdodgekmp.shared.game.GameRules

fun createGuestService(): GuestService = GuestServiceImpl(GUEST_WASM_BYTES)

fun createGameRules(): GameRules = ChasmGameRules(GUEST_WASM_BYTES)
