package com.paintedmap.ui.screens

import android.content.Intent
import com.paintedmap.ui.components.SearchBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.paintedmap.data.User
import com.paintedmap.ui.components.ActionButton
import com.paintedmap.ui.components.GlassPanel
import com.paintedmap.ui.theme.Dimens

@Composable
fun ProfileScreen(
    user: User,
    innerPadding: androidx.compose.foundation.layout.PaddingValues
) {
    var search by remember { mutableStateOf("") }
    val subscriptions = remember {
        listOf(
            "Name 1", "Name 2", "Name 3", "Name 4", "Name 5",
            "Evelina", "Name 7", "Name 8", "Name 9", "Name 10",
            "Name 11", "Name 12", "Name 13", "Name 14", "Name 15"
        )
    }

    val filteredSubscriptions = remember(search, subscriptions) {
        subscriptions.filter {
            it.contains(search, ignoreCase = true)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = Dimens.xs,
            end = Dimens.xs,
            top = Dimens.xs,
            bottom = innerPadding.calculateBottomPadding() + Dimens.xs
        ),
        horizontalArrangement = Arrangement.spacedBy(Dimens.xs),
        verticalArrangement = Arrangement.spacedBy(Dimens.xs)
    ) {
        item (
            span = { GridItemSpan(maxLineSpan) }
        ) {
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(136.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                ProfileBar(user)
            }
        }

        stickyHeader {
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.searchBarHeight),
                color = MaterialTheme.colorScheme.secondary,
                alpha = 1f
            ) {
                SearchBar(
                    search = search,
                    onSearchChange = { search = it }
                )
            }
        }

        itemsIndexed(filteredSubscriptions) { _, subscription ->
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                //shape = MaterialTheme.shapes.large,
                color = lerp(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.background, 0.5f),
                ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subscription,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileBar(user: User) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.md),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.userName,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    //overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = "@${user.userNickName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    maxLines = 1,
                    //overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            ActionButton(
                modifier = Modifier.size(44.dp),
                onClick = {
                    val link = "paintedmap://user/${user.userId}"
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, link)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Поделиться профилем"))
                },
                icon = Icons.Rounded.Share,
                contentDescription = "Поделиться профилем",
                containerColor = Color.Transparent,
                iconColor = MaterialTheme.colorScheme.onPrimary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "67",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "регионы",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Normal
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "123.4k",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "подписчики",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Normal
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "113",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "подписки",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
