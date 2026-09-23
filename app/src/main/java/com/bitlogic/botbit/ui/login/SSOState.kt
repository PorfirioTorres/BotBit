package com.bitlogic.botbit.ui.login

import com.google.firebase.auth.FirebaseUser

sealed interface SSOState {
    data object Idle : SSOState
    data object CheckingExistingSession : SSOState
    data class Authenticated(val user: FirebaseUser) : SSOState
    data class Error(val message: String) : SSOState
}
