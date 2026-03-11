package com.vector.verevcodex.presentation.app

internal sealed interface AppRootDestination {
    data object Loading : AppRootDestination
    data class Auth(
        val startDestination: String,
        val flowKey: Int,
    ) : AppRootDestination

    data object Unlock : AppRootDestination
    data object Merchant : AppRootDestination
}
