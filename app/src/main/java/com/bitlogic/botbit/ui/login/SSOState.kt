package com.bitlogic.botbit.ui.login

import com.google.firebase.auth.FirebaseUser

sealed interface SSOState {
    data object Idle : SSOState
    data object CheckingExistingSession : SSOState
    data class Authenticated(val user: FirebaseUser) : SSOState

    /**
     * Jugando sin cuenta. El progreso se guarda solo en el dispositivo.
     *
     * Existe porque el login era un muro duro: sin cuenta de Google no se podia
     * entrar al juego. Si Play Services falla, si el emulador no tiene cuenta o
     * si no hay internet, el juego entero quedaba inalcanzable.
     */
    data object Guest : SSOState

    data class Error(val message: String) : SSOState

    /** Estados con los que se puede entrar al juego. */
    val canPlay: Boolean get() = this is Authenticated || this is Guest
}
