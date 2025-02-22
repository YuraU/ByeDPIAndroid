package io.github.dovecoteescapee.byedpi.feature.connection.ui.presentation.permissionrequest

import android.app.Dialog
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.dovecoteescapee.byedpi.common.ui.theme.ByeDPITheme
import io.github.dovecoteescapee.byedpi.feature.connection.ui.R

class AskVpnPermissionFragment : BottomSheetFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext()).apply {
            setContentView(
                ComposeView(requireContext()).apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                    setContent {
                        ByeDPITheme { // Добавили тему
                            PermissionsScreen(
                                onBackClick = { dismiss() },
                                onContinueClick = {
                                    dismiss()
                                    parentFragmentManager.setFragmentResult(
                                        "bottom_sheet_result",
                                        bundleOf("continue" to true)
                                    )
                                },
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun PermissionsScreen(
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground  // Цвет текста на фоне
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface  // Цвет текста на поверхности
    val arrowColor = MaterialTheme.colorScheme.onBackground  // Цвет стрелки

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(6.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .padding(6.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                contentDescription = "Back",
                tint = arrowColor
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = null,
                modifier = Modifier.size(60.dp)
            )

            Text(
                text = stringResource(id = R.string.main_ask_for_permissions_header),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = onBackgroundColor,
                modifier = Modifier.padding(top = 24.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .widthIn(max = 300.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.main_ask_for_permissions_description),
                    textAlign = TextAlign.Center,
                    color = onSurfaceColor  // Устанавливаем цвет текста для описания
                )

                Text(
                    text = "",
                    color = primaryColor,  // Здесь оставляем цвет primary для специального текста
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .alpha(0f)
                )

                Button(
                    onClick = onContinueClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    ),
                    shape = RoundedCornerShape(4.dp),  // Уменьшаем закругление
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.universal_action_continue),
                        fontSize = 14.sp,
                        color = Color.White  // Белый цвет для текста на кнопке
                    )
                }
            }
        }
    }
}
