package com.vector.verevcodex.presentation.stores

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.presentation.components.StateContent
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
fun StoreSelectionScreen(
    onSelected: () -> Unit,
    viewModel: StoreViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize().background(VerevColors.AppBackground)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(VerevColors.Forest, VerevColors.Moss)))
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Select Branch", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text("Choose the location you want to manage today.", color = Color.White.copy(alpha = 0.75f))
            }
        }
        StateContent(state = state) { stores ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(stores) { store ->
                    Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = VerevColors.Gold)
                            Text(store.name, style = MaterialTheme.typography.titleLarge)
                            Text(store.category, color = VerevColors.Moss)
                            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.LocationOn, null, tint = VerevColors.MutedText)
                                Text(store.address, color = VerevColors.MutedText, modifier = Modifier.weight(1f))
                            }
                            Button(
                                onClick = { viewModel.selectStore(store.id, onSelected) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (store.active) VerevColors.Gold else VerevColors.Inactive)
                            ) {
                                Text(if (store.active) "Open Branch" else "Disabled")
                            }
                        }
                    }
                }
            }
        }
    }
}
