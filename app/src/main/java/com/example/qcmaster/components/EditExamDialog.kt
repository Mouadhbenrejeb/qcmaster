package com.example.qcmaster.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.qcmaster.models.Exam

@Composable
fun EditExamDialog(
    exam: Exam,
    availableClasses: List<String>,
    selectedClasses: List<String>,
    classSelectionError: String? = null,
    onDismiss: () -> Unit,
    onClassToggled: (String, Boolean) -> Unit,
    onUpdate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Edit Exam",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            ) 
        },
        text = {
            Column {
                // Display exam name (read-only)
                OutlinedTextField(
                    value = exam.name,
                    onValueChange = { /* Read-only */ },
                    label = { Text("Exam Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false // Disable name editing
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Assign classes to this exam:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (availableClasses.isEmpty()) {
                    Text(
                        text = "No classes available. Please create classes first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        items(availableClasses) { className ->
                            val isSelected = selectedClasses.contains(className)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        onClassToggled(className, checked)
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = className,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    if (classSelectionError != null) {
                        Text(
                            text = classSelectionError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onUpdate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}