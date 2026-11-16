package com.paintedmap.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.paintedmap.domain.model.NetworkError
import com.paintedmap.ui.theme.Dimens


@Composable
fun NetworkErrorBlock(
    error: NetworkError,
    message: String,
    isRetryBlocked: Boolean,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(Dimens.sm))

        when (error) {
            NetworkError.NotFound -> Unit
            NetworkError.Unauthorized -> {
                Button(onClick = onRetry) {
                    Text("Войти заново")
                }
            }
            is NetworkError.Conflict -> {
                if (error.reloadAllowed) {
                    Button(onClick = onRetry) {
                        Text("Обновить данные")
                    }
                }
            }
            is NetworkError.RateLimited -> {
                Button(onClick = onRetry, enabled = !isRetryBlocked) {
                    Text(if (isRetryBlocked) "Подождите..." else "Повторить")
                }
            }
            NetworkError.Unavailable,
            NetworkError.InvalidData -> {
                Button(onClick = onRetry) {
                    Text("Повторить")
                }
            }
        }
    }
}