package com.vector.verevcodex.presentation.merchant.common

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.auth.AuthUser
import com.vector.verevcodex.domain.model.business.Store
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
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                ambientColor = VerevColors.Forest.copy(alpha = 0.08f),
                spotColor = VerevColors.Forest.copy(alpha = 0.08f),
            )
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF0A2F24),
                        VerevColors.Forest,
                        Color(0xFF1A5C47),
                    ),
                ),
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
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
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(VerevColors.Gold, VerevColors.Tan))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp),
                    )
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
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = VerevColors.Forest,
                            )
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
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedStore?.name ?: stringResource(R.string.merchant_select_store),
                            color = Color.White,
                            style = TextStyle(
                                fontSize = 13.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                        val subtitle = when {
                            selectedStore == null -> null
                            selectedStore.active -> stringResource(R.string.merchant_store_active)
                            else -> stringResource(R.string.merchant_store_disabled)
                        }
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                color = if (selectedStore?.active == true) VerevColors.Gold else Color.White.copy(alpha = 0.55f),
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                            )
                        }
                    }
                    if (stores.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                DropdownMenu(expanded = showStoreMenu, onDismissRequest = { showStoreMenu = false }) {
                    stores.forEach { store ->
                        DropdownMenuItem(
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(store.name, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = if (store.active) {
                                            stringResource(R.string.merchant_store_active)
                                        } else {
                                            stringResource(R.string.merchant_store_disabled)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = VerevColors.Forest.copy(alpha = 0.6f),
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = VerevColors.Forest,
                                )
                            },
                            trailingIcon = if (selectedStore?.id == store.id) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = VerevColors.Moss,
                                    )
                                }
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
                    .clickable { }
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 1.dp, end = 1.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(VerevColors.Gold),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "3",
                        color = VerevColors.Forest,
                        style = TextStyle(
                            fontSize = 10.sp,
                            lineHeight = 10.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            destinations.forEach { destination ->
                MerchantBottomBarItem(
                    destination = destination,
                    selected = currentRoute == destination.route,
                    onClick = { onDestinationClick(destination) },
                )
            }
        }
    }
}

@Composable
private fun RowScope.MerchantBottomBarItem(
    destination: MerchantBottomDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val activeTint = VerevColors.Gold
    val inactiveTint = Color(0xFF9CA3AF)
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(if (selected) activeTint.copy(alpha = 0.10f) else Color.Transparent)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = stringResource(destination.labelRes),
            tint = if (selected) activeTint else inactiveTint,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = stringResource(destination.labelRes),
            color = if (selected) activeTint else inactiveTint,
            style = TextStyle(
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}
