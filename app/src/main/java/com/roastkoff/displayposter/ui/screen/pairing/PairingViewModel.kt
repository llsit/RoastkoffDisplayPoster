package com.roastkoff.displayposter.ui.screen.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roastkoff.displayposter.common.DisplayPreferences
import com.roastkoff.displayposter.repository.PairingRepository
import com.roastkoff.displayposter.repository.PairingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PairingUiState(
    val code: String? = null,
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val isPaired: Boolean = false
)

@HiltViewModel
class PairingViewModel @Inject constructor(
    private val pairingRepository: PairingRepository,
    private val prefs: DisplayPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairingUiState())
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

    private var listenJob: Job? = null

    init {
        checkingPairingDisplay()
    }

    fun checkingPairingDisplay() {
        viewModelScope.launch {
            val alreadyPaired = prefs.isPaired()
            if (alreadyPaired) {
                _uiState.update {
                    it.copy(
                        code = null,
                        loading = false,
                        errorMessage = null,
                        isPaired = true
                    )
                }
                return@launch
            }

            val savedCode = prefs.getPairingCode()
            if (savedCode != null) {
                _uiState.update {
                    it.copy(
                        code = savedCode,
                        loading = false,
                        errorMessage = null,
                        isPaired = false
                    )
                }
                startListenPairing(savedCode)
            } else {
                createNewSession()
            }
        }
    }

    private fun createNewSession() {
        viewModelScope.launch {
            listenJob?.cancel()

            _uiState.update { it.copy(loading = true, errorMessage = null, isPaired = false) }

            runCatching {
                pairingRepository.createPairingSession()
            }.onSuccess { code ->
                prefs.savePairingCode(code)

                _uiState.update {
                    it.copy(
                        code = code,
                        loading = false,
                        errorMessage = null
                    )
                }

                startListenPairing(code)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = e.message ?: "เกิดข้อผิดพลาดขณะสร้างโค้ด"
                    )
                }
            }
        }
    }

    private fun startListenPairing(code: String) {
        listenJob?.cancel()
        listenJob = viewModelScope.launch {
            pairingRepository.listenPairing(code).collect { result ->
                when (result) {
                    is PairingResult.Waiting -> {

                    }

                    is PairingResult.Claimed -> {
                        viewModelScope.launch {
                            prefs.saveDisplayInfo(
                                tenantId = result.info.tenantId,
                                groupId = result.info.groupId,
                                displayId = result.info.displayId
                            )
                        }
                        _uiState.update { it.copy(isPaired = true) }
                    }

                    is PairingResult.Error -> {
                        _uiState.update { it.copy(errorMessage = result.message) }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenJob?.cancel()
    }
}
