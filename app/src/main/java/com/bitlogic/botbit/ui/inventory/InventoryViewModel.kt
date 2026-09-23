package com.bitlogic.botbit.ui.inventory

import androidx.lifecycle.ViewModel
import com.bitlogic.botbit.data.CharacterData
import com.bitlogic.botbit.data.Characters
import com.bitlogic.botbit.data.ProgressStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val store: ProgressStore  // INYECTAR ProgressStore
) : ViewModel() {
    
    private val _characters = MutableStateFlow(Characters.all.map { character ->
        character.copy(
            locked = !store.isCharacterUnlocked(character.id)
        )
    })
    val characters: StateFlow<List<CharacterData>> = _characters.asStateFlow()
    
    private val _selectedCharacter = MutableStateFlow(store.getSelectedCharacter())
    val selectedCharacter: StateFlow<String> = _selectedCharacter.asStateFlow()

    private val _totalCoins = MutableStateFlow(store.totalCoins)
    val totalCoins: StateFlow<Int> = _totalCoins.asStateFlow()
    
    private val _selectionConfirmed = MutableStateFlow(false)
    val selectionConfirmed: StateFlow<Boolean> = _selectionConfirmed.asStateFlow()
    
    init {
        // Sincronizar selección con store
        _selectedCharacter.value = store.getSelectedCharacter()
    }
    
    fun selectCharacter(id: String) {
        val character = characters.value.find { it.id == id }
        if (character != null && !character.locked) {
            _selectedCharacter.value = id
        }
    }
    
    fun confirmSelection() {
        _selectionConfirmed.value = true
        store.saveSelectedCharacter(_selectedCharacter.value)  // GUARDAR
        resetConfirmation()
    }
    
    fun resetConfirmation() {
        _selectionConfirmed.value = false
    }
    
    fun buyCharacter(characterId: String) {
        val character = characters.value.find { it.id == characterId }
        if (character != null && character.locked) {
            // Cobrar monedas reales del monedero global
            if (store.spendCoins(character.price)) {
                // Actualizar el estado del saldo para la UI
                _totalCoins.value = store.totalCoins
                
                // Desbloquear en store
                store.unlockCharacter(characterId)

                // Actualizar lista de personajes (quitar candado)
                _characters.value = _characters.value.map {
                    if (it.id == characterId) {
                        it.copy(locked = false)
                    } else {
                        it
                    }
                }
            }
        }
    }
}
