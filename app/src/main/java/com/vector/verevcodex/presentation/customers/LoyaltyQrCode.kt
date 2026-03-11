package com.vector.verevcodex.presentation.customers

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

@Composable
internal fun LoyaltyQrCode(
    value: String,
    modifier: Modifier = Modifier,
) {
    val bitmap = remember(value) { createQrBitmap(value) }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

internal fun createQrBitmap(value: String, size: Int = 1024): Bitmap {
    val matrix: BitMatrix = MultiFormatWriter().encode(value, BarcodeFormat.QR_CODE, size, size)
    val pixels = IntArray(size * size)
    for (y in 0 until size) {
        for (x in 0 until size) {
            pixels[y * size + x] = if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        }
    }
    return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
        setPixels(pixels, 0, size, 0, 0, size, size)
    }
}
