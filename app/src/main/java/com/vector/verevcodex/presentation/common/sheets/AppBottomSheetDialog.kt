package com.vector.verevcodex.presentation.common.sheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheetDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    allowSwipeToDismiss: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
    content: @Composable ColumnScope.(dismiss: () -> Unit, dismissAfter: (() -> Unit) -> Unit) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { target ->
            allowSwipeToDismiss || target != SheetValue.Hidden
        },
    )
    val scope = rememberCoroutineScope()

    fun dismissSheet(after: (() -> Unit)? = null) {
        scope.launch {
            sheetState.hide()
            after?.invoke()
            onDismissRequest()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (allowSwipeToDismiss) {
                dismissSheet()
            }
        },
        sheetState = sheetState,
        sheetGesturesEnabled = allowSwipeToDismiss,
        dragHandle = null,
        sheetMaxWidth = Dp.Unspecified,
        containerColor = Color.Transparent,
        scrimColor = Color.Black.copy(alpha = 0.28f),
        tonalElevation = 0.dp,
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White,
            shadowElevation = 18.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(contentPadding),
            ) {
                content(
                    { dismissSheet() },
                    { after -> dismissSheet(after) },
                )
            }
        }
    }
}
