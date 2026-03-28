package com.vector.verevcodex.common.errors

class AppStateException(
    val reason: Reason,
) : IllegalStateException(reason.message) {
    enum class Reason(val message: String) {
        NoActiveAccount("No active account"),
        NoActiveSession("No active session"),
    }
}
