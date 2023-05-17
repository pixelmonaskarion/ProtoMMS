package io.github.pixelmonaskarion.protomms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.github.pixelmonaskarion.protomms.proto.ProtoMms.Message
import io.github.pixelmonaskarion.protomms.ui.theme.ProtoMMSTheme


class MainActivity : ComponentActivity() {
    val startApp = {
        setContent {
            ProtoMMSTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    ConversationMessages(getInbox())
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

@Composable
fun Conversations(threads: List<Thread>) {
    LazyColumn {
        items(threads) { thread ->
            val senderIds = ArrayList<Contact>();
            thread.recipients.split(" ").iterator().forEach {
                val contact = getContactById(it.toLong());
                if (contact != null) {
                    senderIds.add(contact);
                } else {
                    senderIds.add(Contact(it.toLong(), "🤷", null))
                }
            }
            var title = "";
            senderIds.forEach {
                title += it.displayName + ", ";
            }
            title.substring(0, title.lastIndex-2);
            var icon = painterResource(R.drawable.profile_picture)
            senderIds.forEach { 
                if (it.pfp_uri != null) {
                }
            }
            ConversationPreview(title = title, lastMessage = Message("I haven't quite figured this out yet :/", arrayOf(Address("Chrissy"))), icon = icon)
        }
    }
}

@Composable
fun ConversationPreview(title: String, lastMessage: Message, icon: Painter) {
    Row(modifier = Modifier.padding(all = 8.dp)) {
        Image(
            painter = icon,
            contentDescription = "Conversation profile Picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = lastMessage.sender.address+": "+lastMessage.text,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview
@Composable
fun PreviewConversationPreview() {
    ProtoMMSTheme {
        ConversationPreview(
            "Funny GC",
            Message("Chrissyyyy", arrayOf(Address("Miles"))),
            painterResource(R.drawable.profile_picture)
        )
    }
}


@Composable
fun ConversationMessages(messages: ArrayList<Message>) {
    LazyColumn {
        items(messages) { message ->
            MessageCard(message)
        }
    }
}

@Preview
@Composable
fun PreviewConversationMessages() {
    ProtoMMSTheme {
        ConversationMessages(ArrayList(listOf(Message("I love android 🤓🤓🤓", arrayOf(Address("Lexi 😡😡😡"))))))
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageCard(msg: Message) {
    Row(modifier = Modifier.padding(all = 8.dp)) {
        Image(
            painter = painterResource(R.drawable.profile_picture),
            contentDescription = "Contact profile Picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        var expandedState by remember { mutableStateOf(0) }
        val surfaceColor by animateColorAsState(
            if (expandedState > 0) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant
        )
        var messageInput by remember { mutableStateOf("") }

        Column(
            modifier = Modifier.clickable {
                expandedState = if (expandedState > 0) {
                    0
                } else {
                    1
                }
            }
        ) {
            val authorDisplayName = msg.sender.address
            Text(
                text = authorDisplayName,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                val screenWidth = LocalConfiguration.current.screenWidthDp
                Column {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        shadowElevation = 1.dp,
                        color = surfaceColor,
                        modifier = Modifier
                            .animateContentSize()
                            .padding(1.dp)
                            .widthIn(min = 0.dp, max = screenWidth.dp - 100.dp)
                    ) {
                            Text(
                                text = msg.text,
                                modifier = Modifier.padding(all = 4.dp),
                                maxLines = if (expandedState > 0) Int.MAX_VALUE else 1,
                                style = MaterialTheme.typography.bodyMedium,
                            )

                    }
                    if (expandedState == 2) {
                        TextField(
                            value = messageInput,
                            onValueChange = { messageInput = it },
                            modifier = Modifier.padding(5.dp),
                        )
                    }
                }
                Button(
                    onClick = {
                        if (expandedState == 2) {
                            sendMessage(Message(messageInput, arrayOf(Address(msg.sender.address))))
                            messageInput = ""
                        } else {
                            expandedState = 2
                        }
                    },
                    modifier = Modifier
                        .width(30.dp)
                        .height(30.dp),
                ) {
                    Text("Reply")
                }
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun PreviewMessageCard() {
    ProtoMMSTheme {
        Surface {
            MessageCard(
                msg = Message("Hey, take a look at Jetpack Compose, it's great!", arrayOf(Address("Lexi")))
            )
        }
    }
}