package com.chrissytopher.pumpmessager.ui.conversation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputBox(
    onSend: (String, Uri?) -> Unit
) {
    val (message, setMessage) = remember { mutableStateOf("") }
    var fileUri: Uri? by remember { mutableStateOf(null) }
    val pickPictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { imageUri ->
        if (imageUri != null) {
            fileUri = imageUri
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(
            onClick = {
                pickPictureLauncher.launch("*/*")
            }
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "File Upload Button"
            )
        }
        OutlinedTextField(
            value = message,
            onValueChange = setMessage,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            placeholder = { Text(text = "Type a message") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    onSend(message, fileUri)
                    setMessage("")
                }
            )
        )
        IconButton(
            onClick = {
                onSend(message, fileUri)
                setMessage("")
            }
        ) {
            Icon(
                Icons.Filled.Send,
                contentDescription = "Send Button"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessageInputBoxPreview() {
    MessageInputBox(onSend = { _, _ -> })
}