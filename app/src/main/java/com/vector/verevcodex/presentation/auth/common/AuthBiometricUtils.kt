package com.vector.verevcodex.presentation.auth.common

import androidx.annotation.StringRes
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

fun showBiometricPrompt(
    activity: FragmentActivity,
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int,
    @StringRes negativeRes: Int,
    onResult: (Boolean) -> Unit,
) {
    val biometricManager = BiometricManager.from(activity)
    val authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK
    if (biometricManager.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
        onResult(false)
        return
    }
    val executor: Executor = ContextCompat.getMainExecutor(activity)
    try {
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onResult(true)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onResult(false)
                }

                override fun onAuthenticationFailed() = Unit
            },
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(titleRes))
            .setSubtitle(activity.getString(subtitleRes))
            .setConfirmationRequired(false)
            .setAllowedAuthenticators(authenticators)
            .setNegativeButtonText(activity.getString(negativeRes))
            .build()
        prompt.authenticate(promptInfo)
    } catch (_: IllegalArgumentException) {
        onResult(false)
    }
}
