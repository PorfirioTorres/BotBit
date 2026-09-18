package com.bitlogic.botbit.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitlogic.botbit.data.repository.SSORepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SSOViewModel @Inject constructor(
    private val repository: SSORepositoryImpl
) : ViewModel() {

    private val _ssoState = MutableStateFlow<SSOState>(SSOState.Idle)
    val ssoState: StateFlow<SSOState> = _ssoState.asStateFlow()

    fun checkExistingSession(context: Context) {
        val currentUser = repository.getCurrentUser()
        if (currentUser != null) {
            _ssoState.value = SSOState.Authenticated(currentUser)
        } else {
            viewModelScope.launch {
                _ssoState.value = SSOState.CheckingExistingSession
                repository.silentSSO(context).onSuccess { user ->
                    _ssoState.value = SSOState.Authenticated(user)
                }.onFailure {
                    _ssoState.value = SSOState.Idle
                }
            }
        }
    }

    fun initiateInteractiveSSO(context: Context) {
        viewModelScope.launch {
            _ssoState.value = SSOState.CheckingExistingSession
            repository.interactiveSSO(context).onSuccess { user ->
                _ssoState.value = SSOState.Authenticated(user)
            }.onFailure { error ->
                _ssoState.value = SSOState.Error(error.message ?: "Error desconocido")
            }
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            repository.signOut(context)
            _ssoState.value = SSOState.Idle
        }
    }
}
