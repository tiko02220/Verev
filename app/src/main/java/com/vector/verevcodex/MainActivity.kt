package com.vector.verevcodex

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.platform.nfc.NfcCardPayloadParser
import com.vector.verevcodex.platform.nfc.NfcCardWriteCoordinator
import com.vector.verevcodex.platform.wallet.GoogleWalletProvisioningManager
import com.vector.verevcodex.presentation.app.AppEntryScreen
import com.vector.verevcodex.presentation.auth.security.AppSecurityViewModel
import com.vector.verevcodex.presentation.scan.ScanViewModel
import com.vector.verevcodex.presentation.theme.VerevMerchantTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val scanViewModel: ScanViewModel by viewModels()
    private val appSecurityViewModel: AppSecurityViewModel by viewModels()
    private var shouldRelockOnPause = false
    private var didEnterBackground = false
    private var suppressRelockUntilResume = false
    private var pendingExternalNfcResult: PendingExternalNfcResult? = null

    @Inject lateinit var nfcCardWriteCoordinator: NfcCardWriteCoordinator
    @Inject lateinit var googleWalletProvisioningManager: GoogleWalletProvisioningManager

    private val nfcAdapter: NfcAdapter? by lazy { NfcAdapter.getDefaultAdapter(this) }
    private val nfcPendingIntent: PendingIntent by lazy {
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appSecurityViewModel.uiState.collect { state ->
                    if (!state.requiresUnlock && state.authEntryDestination == null) {
                        deliverPendingExternalNfcResult()
                    }
                }
            }
        }
        handleNfcIntent(intent)
        setContentView(
            ComposeView(this).apply {
                setContent {
                    VerevMerchantTheme {
                        val securityState by appSecurityViewModel.uiState.collectAsStateWithLifecycle()
                        AppEntryScreen(
                            securityState = securityState,
                            scanViewModel = scanViewModel,
                            onPinChanged = appSecurityViewModel::updatePinCode,
                            onUseBiometric = appSecurityViewModel::requestBiometric,
                            onBiometricResult = appSecurityViewModel::biometricHandled,
                            onRecoverAccess = appSecurityViewModel::recoverAccess,
                            onLogout = appSecurityViewModel::logoutToLogin,
                        )
                    }
                }
            }
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (googleWalletProvisioningManager.handleActivityResult(requestCode, resultCode)) {
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        suppressRelockUntilResume = false
        nfcAdapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
    }

    override fun onPause() {
        if ((shouldRelockOnPause || isFinishing) && !isChangingConfigurations && !suppressRelockUntilResume) {
            appSecurityViewModel.onAppBackgrounded()
        }
        nfcAdapter?.disableForegroundDispatch(this)
        super.onPause()
    }

    override fun onStop() {
        if (!isChangingConfigurations && !isFinishing && shouldRelockOnPause && !suppressRelockUntilResume) {
            appSecurityViewModel.onAppBackgrounded()
            didEnterBackground = true
        }
        super.onStop()
    }

    override fun onUserLeaveHint() {
        shouldRelockOnPause = true
        super.onUserLeaveHint()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && didEnterBackground) {
            appSecurityViewModel.onAppForegrounded()
            shouldRelockOnPause = false
            didEnterBackground = false
        }
    }

    fun suppressRelockForTransientSystemUi() {
        suppressRelockUntilResume = true
    }

    private fun handleNfcIntent(intent: Intent?) {
        val action = intent?.action ?: return
        if (action == NfcAdapter.ACTION_TAG_DISCOVERED || action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            if (nfcCardWriteCoordinator.handleIntent(intent)) return
            val tagId = NfcCardPayloadParser.extractLoyaltyId(intent)
            if (appSecurityViewModel.uiState.value.requiresUnlock) {
                pendingExternalNfcResult = if (tagId.isNullOrBlank()) {
                    PendingExternalNfcResult.Failure
                } else {
                    PendingExternalNfcResult.Success(tagId)
                }
                return
            }
            if (tagId.isNullOrBlank()) {
                scanViewModel.onNfcScanFailed()
                return
            }
            scanViewModel.onExternalNfcScan(tagId)
        }
    }

    private fun deliverPendingExternalNfcResult() {
        when (val pendingResult = pendingExternalNfcResult ?: return) {
            PendingExternalNfcResult.Failure -> scanViewModel.onNfcScanFailed()
            is PendingExternalNfcResult.Success -> scanViewModel.onExternalNfcScan(pendingResult.loyaltyId)
        }
        pendingExternalNfcResult = null
    }

    private sealed interface PendingExternalNfcResult {
        data object Failure : PendingExternalNfcResult
        data class Success(val loyaltyId: String) : PendingExternalNfcResult
    }
}
