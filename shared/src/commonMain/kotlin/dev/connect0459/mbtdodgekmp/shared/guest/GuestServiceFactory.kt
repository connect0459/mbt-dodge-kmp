package dev.connect0459.mbtdodgekmp.shared.guest

fun createGuestService(): GuestService = GuestServiceImpl(GUEST_WASM_BYTES)
