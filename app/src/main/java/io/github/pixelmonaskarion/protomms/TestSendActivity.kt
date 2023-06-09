package io.github.pixelmonaskarion.protomms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import io.github.pixelmonaskarion.protomms.proto.ProtoMms.Attachment
import io.github.pixelmonaskarion.protomms.ui.theme.ProtoMMSTheme
import kotlinx.coroutines.runBlocking

class TestSendActivity : ComponentActivity() {
    val startApp = {
        setContent {
            ProtoMMSTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    SendMessage()
                }
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted: Map<String,Boolean> ->
            if (isGranted.containsValue(false)) {
                Log.e("ProtoMMS", isGranted.toString())
                setContent {
                    Text(text = "was denied")
                }
            } else {
                startApp()
            }
        }

    private fun hasPermission(context: Context, permissionStr: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permissionStr
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermissions(permissions: ArrayList<String>, context: Context) {
        var notGrantedPermissions = ArrayList<String>()
        while (permissions.isNotEmpty()) {
            val permission = permissions[permissions.size-1]
            permissions.removeAt(permissions.size-1)
            if (!hasPermission(context, permission)) {
                notGrantedPermissions.add(permission)
            }
        }
        requestPermissionLauncher.launch(notGrantedPermissions.toArray(arrayOf()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init(contentResolver, this)
        checkPermissions(arrayListOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_NUMBERS), applicationContext)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SendMessage() {
    var recipient by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var fileUri: Uri? by remember { mutableStateOf(null) }
    val pickPictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { imageUri ->
        if (imageUri != null) {
            fileUri = imageUri
        }
    }

    Column() {
        Text(text = "Your phone number: " + getPhoneNumber())
        TextField(value = recipient, onValueChange = {recipient = it}, label = {Text(text = "Recipient")}, modifier = Modifier.padding(5.dp))
        TextField(value = text, onValueChange = {text = it}, label = {Text(text = "Message")}, modifier = Modifier.padding(5.dp))
        Button(onClick = {
            pickPictureLauncher.launch("*/*")
        }) {
            Text(text = "Pick File")
        }
        Button(onClick = {
            var attachments: Array<Attachment> = arrayOf();
            if (fileUri != null) {
                attachments = arrayOf(Attachment(fileUri!!))
            }
            runBlocking {
                sendMessage(Message(text, arrayOf(Address(recipient)), attachments))
            }
            text = ""
        }) {
            Text(text = "Send")
        }
    }
}