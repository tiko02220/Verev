package com.vector.verevcodex.presentation.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.presentation.components.LabelValueRow
import com.vector.verevcodex.presentation.components.StateContent
import com.vector.verevcodex.presentation.settings.SettingsBackRow
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
fun TransactionEntryScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    viewModel: TransactionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize().background(VerevColors.AppBackground)) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = contentPadding.calculateTopPadding() + 16.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SettingsBackRow(onBack = onBack)
            Text("Transaction Entry", style = MaterialTheme.typography.headlineMedium)
            Button(
                onClick = viewModel::addDemoTransaction,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VerevColors.Gold)
            ) {
                Text("Record demo purchase")
            }
        }
        StateContent(state = state) { transactions ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = contentPadding.calculateBottomPadding() + 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(transactions.take(8)) { transaction ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(transaction.metadata, style = MaterialTheme.typography.titleLarge)
                            LabelValueRow("Amount", "${transaction.amount.toInt()} AMD")
                            LabelValueRow("Points earned", transaction.pointsEarned.toString())
                            LabelValueRow("Redeemed", transaction.pointsRedeemed.toString())
                        }
                    }
                }
            }
        }
    }
}
