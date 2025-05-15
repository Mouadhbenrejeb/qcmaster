package com.example.qcmaster.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.qcmaster.ui.theme.QcmasterTheme

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isOutlined: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        if (isOutlined) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = label?.let { { Text(text = it) } },
                placeholder = placeholder?.let { { Text(text = it) } },
                isError = isError,
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                singleLine = singleLine,
                maxLines = maxLines,
                enabled = enabled,
                shape = RoundedCornerShape(8.dp)
            )
        } else {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = label?.let { { Text(text = it) } },
                placeholder = placeholder?.let { { Text(text = it) } },
                isError = isError,
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                singleLine = singleLine,
                maxLines = maxLines,
                enabled = enabled,
                shape = RoundedCornerShape(8.dp)
            )
        }

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Preview(name = "Light Mode")
@Composable
private fun TextFieldPreviewLight() {
    QcmasterTheme(darkTheme = false) {
        var text by remember { mutableStateOf("") }
        var errorText by remember { mutableStateOf("") }

        Column(modifier = Modifier.padding(16.dp)) {
            // Standard filled text field
            AppTextField(
                value = text,
                onValueChange = { text = it },
                label = "Standard TextField",
                placeholder = "Enter text here",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Outlined text field
            AppTextField(
                value = text,
                onValueChange = { text = it },
                label = "Outlined TextField",
                placeholder = "Enter text here",
                isOutlined = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error state text field
            AppTextField(
                value = errorText,
                onValueChange = { errorText = it },
                label = "Error TextField",
                placeholder = "This field has an error",
                isError = true,
                errorMessage = "This is an error message",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Disabled text field
            AppTextField(
                value = "Disabled text field",
                onValueChange = { },
                label = "Disabled TextField",
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(name = "Dark Mode")
@Composable
private fun TextFieldPreviewDark() {
    QcmasterTheme(darkTheme = true) {
        var text by remember { mutableStateOf("") }
        var errorText by remember { mutableStateOf("") }

        Column(modifier = Modifier.padding(16.dp)) {
            // Standard filled text field
            AppTextField(
                value = text,
                onValueChange = { text = it },
                label = "Standard TextField",
                placeholder = "Enter text here",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Outlined text field
            AppTextField(
                value = text,
                onValueChange = { text = it },
                label = "Outlined TextField",
                placeholder = "Enter text here",
                isOutlined = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error state text field
            AppTextField(
                value = errorText,
                onValueChange = { errorText = it },
                label = "Error TextField",
                placeholder = "This field has an error",
                isError = true,
                errorMessage = "This is an error message",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Disabled text field
            AppTextField(
                value = "Disabled text field",
                onValueChange = { },
                label = "Disabled TextField",
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
