package com.vector.verevcodex.presentation.programs

import androidx.annotation.StringRes

internal data class ProgramQuickActionUi(
    @StringRes val labelRes: Int,
    val onClick: () -> Unit,
)
