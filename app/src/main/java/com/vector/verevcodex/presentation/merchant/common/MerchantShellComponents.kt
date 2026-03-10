package com.vector.verevcodex.presentation.merchant.common

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.Store
import com.vector.verevcodex.domain.model.auth.AuthUser
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
fun MerchantTopBar(
    currentUser: AuthUser?,
    selectedStore: Store?,
    stores: List<Store>,
    onStoreSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showUserMenu by remember { mutableStateOf(false) }
    var showStoreMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(VerevColors.ForestDeep, VerevColors.Forest, Color(0xFF1A5C47))))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showUserMenu = true }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(VerevColors.Gold, VerevColors.Tan))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White)
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                }
                DropdownMenu(expanded = showUserMenu, onDismissRequest = { showUserMenu = false }) {
                    DropdownMenuItem(
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = currentUser?.fullName.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = currentUser?.email.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VerevColors.Forest.copy(alpha = 0.6f),
                                )
                            }
                        },
                        onClick = { showUserMenu = false },
                        leadingIcon = {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = VerevColors.Forest)
                        },
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(enabled = stores.isNotEmpty()) { showStoreMenu = true }
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.84f),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedStore?.name ?: stringResource(R.string.merchant_select_store),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = selectedStore?.category ?: stringResource(R.string.merchant_business_location),
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                }
                DropdownMenu(expanded = showStoreMenu, onDismissRequest = { showStoreMenu = false }) {
                    stores.forEach { store ->
                        DropdownMenuItem(
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(store.name, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = if (store.active) stringResource(R.string.merchant_store_active) else stringResource(R.string.merchant_store_disabled),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = VerevColors.Forest.copy(alpha = 0.6f),
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = VerevColors.Forest)
                            },
                            trailingIcon = if (selectedStore?.id == store.id) {
                                { Icon(Icons.Default.Check, contentDescription = null, tint = VerevColors.Moss) }
                            } else {
                                null
                            },
                            onClick = {
                                onStoreSelected(store.id)
                                showStoreMenu = false
                            },
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 2.dp, end = 2.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(VerevColors.Gold),
                )
            }
        }
    }
}

data class MerchantBottomDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
)

@Composable
fun MerchantBottomBar(
    destinations: List<MerchantBottomDestination>,
    currentRoute: String?,
    onDestinationClick: (MerchantBottomDestination) -> Unit,
) {
    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier.navigationBarsPadding(),
    ) {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onDestinationClick(destination) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VerevColors.Gold,
                    selectedTextColor = VerevColors.Gold,
                    indicatorColor = VerevColors.Gold.copy(alpha = 0.12f),
                    unselectedIconColor = VerevColors.Inactive,
                    unselectedTextColor = VerevColors.Inactive,
                ),
                icon = { Icon(destination.icon, contentDescription = stringResource(destination.labelRes)) },
                label = { Text(stringResource(destination.labelRes)) },
            )
        }
    }
}
