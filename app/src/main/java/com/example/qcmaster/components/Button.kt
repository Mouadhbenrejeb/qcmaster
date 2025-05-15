package com.example.qcmaster.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.qcmaster.ui.theme.QcmasterTheme

enum class ButtonType {
    PRIMARY,
    SECONDARY,
    OUTLINED,
    TEXT
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.PRIMARY,
    enabled: Boolean = true,
    fullWidth: Boolean = false
) {
    val buttonModifier = if (fullWidth) {
        modifier
            .fillMaxWidth()
            .height(48.dp)
    } else {
        modifier.height(48.dp)
    }

    when (type) {
        ButtonType.PRIMARY -> PrimaryButton(
            text = text,
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled
        )
        ButtonType.SECONDARY -> SecondaryButton(
            text = text,
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled
        )
        ButtonType.OUTLINED -> OutlinedAppButton(
            text = text,
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled
        )
        ButtonType.TEXT -> TextAppButton(
            text = text,
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled
        )
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
private fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
private fun OutlinedAppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
private fun TextAppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
        )
    }
}

@Preview(name = "Light Mode")
@Composable
private fun ButtonPreviewLight() {
    QcmasterTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            AppButton(
                text = "Primary Button",
                onClick = {},
                type = ButtonType.PRIMARY,
                fullWidth = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppButton(
                text = "Secondary Button",
                onClick = {},
                type = ButtonType.SECONDARY,
                fullWidth = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppButton(
                text = "Outlined Button",
                onClick = {},
                type = ButtonType.OUTLINED,
                fullWidth = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppButton(
                text = "Text Button",
                onClick = {},
                type = ButtonType.TEXT,
                fullWidth = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppButton(
                text = "Disabled Button",
                onClick = {},
                enabled = false,
                fullWidth = true
            )
        }
    }
}

@Preview(name = "Dark Mode")
@Composable
private fun ButtonPreviewDark() {
    QcmasterTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(16.dp)) {
            AppButton(
                text = "Primary Button",
                onClick = {},
                type = ButtonType.PRIMARY,
                fullWidth = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppButton(
                text = "Secondary Button",
                onClick = {},
                type = ButtonType.SECONDARY,
                fullWidth = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppButton(
                text = "Outlined Button",
                onClick = {},
                type = ButtonType.OUTLINED,
                fullWidth = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppButton(
                text = "Text Button",
                onClick = {},
                type = ButtonType.TEXT,
                fullWidth = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppButton(
                text = "Disabled Button",
                onClick = {},
                enabled = false,
                fullWidth = true
            )
        }
    }
}
