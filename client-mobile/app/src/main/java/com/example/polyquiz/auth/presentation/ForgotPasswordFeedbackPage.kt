package com.example.polyquiz.auth.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyquiz.auth.domain.AuthViewModel
import com.example.polyquiz.constants.DisplayAuthenticationText

@Composable
fun ForgotPasswordFeedbackPage(
    modifier: Modifier,
    authViewModel: AuthViewModel,
    navigateToLogin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(
                        start = 128.dp,
                        top = 32.dp,
                        end = 128.dp,
                        bottom = 32.dp
                    )
                    .fillMaxWidth(0.5f)
            ) {
                Text(
                    text = DisplayAuthenticationText.SENT_EMAIL.value,
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = DisplayAuthenticationText.SENT_EMAIL_EXTRA_INFO.value,
                    textAlign = TextAlign.Center
                )

                // TODO: Add check icon

                Spacer(modifier = Modifier.height(8.dp))

                ElevatedButton(
                    onClick =
                    {
                        authViewModel.resetAuthState()
                        navigateToLogin()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceBright,
                        contentColor = MaterialTheme.colorScheme.onSurface

                    )
                ) {
                    Text(DisplayAuthenticationText.RETURN_TO_LOGIN.value)
                }
            }
        }
    }
}
