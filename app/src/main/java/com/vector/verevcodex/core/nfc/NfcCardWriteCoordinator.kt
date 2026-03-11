package com.vector.verevcodex.core.nfc

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface NfcCardWriteState {
    data object Idle : NfcCardWriteState
    data class Ready(val request: NfcCardWriteRequest) : NfcCardWriteState
    data class Writing(val request: NfcCardWriteRequest) : NfcCardWriteState
    data class Success(val request: NfcCardWriteRequest) : NfcCardWriteState
    data class Error(val request: NfcCardWriteRequest, val reason: NfcCardWriteError) : NfcCardWriteState
}

@Singleton
class NfcCardWriteCoordinator @Inject constructor() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow<NfcCardWriteState>(NfcCardWriteState.Idle)
    val state: StateFlow<NfcCardWriteState> = _state.asStateFlow()

    fun startWrite(request: NfcCardWriteRequest) {
        _state.value = NfcCardWriteState.Ready(request)
    }

    fun clear() {
        _state.value = NfcCardWriteState.Idle
    }

    fun retry() {
        val current = _state.value
        if (current is NfcCardWriteState.Error) {
            _state.value = NfcCardWriteState.Ready(current.request)
        }
    }

    fun handleIntent(intent: Intent): Boolean {
        val current = _state.value
        val request = when (current) {
            is NfcCardWriteState.Ready -> current.request
            is NfcCardWriteState.Error -> current.request
            else -> return false
        }
        val tag = intent.getParcelableTagExtra() ?: return false
        _state.value = NfcCardWriteState.Writing(request)
        scope.launch {
            runCatching { NfcCardWriter.write(tag, request) }
                .onSuccess {
                    _state.value = NfcCardWriteState.Success(request)
                }
                .onFailure { throwable ->
                    val reason = (throwable as? NfcCardWriteException)?.error ?: NfcCardWriteError.WRITE_FAILED
                    _state.value = NfcCardWriteState.Error(request, reason)
                }
        }
        return true
    }
}

private fun Intent.getParcelableTagExtra(): Tag? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(NfcAdapter.EXTRA_TAG)
    }
