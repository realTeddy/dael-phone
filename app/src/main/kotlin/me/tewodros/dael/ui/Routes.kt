package me.tewodros.dael.ui

object Routes {
    const val HOME = "home"
    const val DIALER = "dialer"
    const val CONTACTS = "contacts"
    const val CALL = "call/{id}"
    const val RANDOM_CALL = "randomcall"
    const val ANIMALS = "animals"
    const val PIANO = "piano"
    const val PAINT = "paint"
    const val PEEKABOO = "peekaboo"
    const val GATE = "gate"
    const val SETTINGS = "settings"

    fun call(id: String) = "call/$id"
}
