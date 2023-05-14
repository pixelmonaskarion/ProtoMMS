package io.github.pixelmonaskarion.xmlsms

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Telephony.Sms.Conversations
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.content.ContextCompat
import io.github.pixelmonaskarion.xmlsms.ui.theme.XMLSMSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init(contentResolver)
        val startApp = {
            setContent {
                XMLSMSTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        ConversationMessages(getInbox())
                    }
                }
            }
        }
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    startApp()
                } else {
                    setContent {
                        XMLSMSTheme {
                            // A surface container using the 'background' color from the theme
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                Text("SMS permission denied")
                            }
                        }
                    }
                }
            }
        if (ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED) {
                    startApp()
                } else {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    requestPermissionLauncher.launch(
                        Manifest.permission.READ_CONTACTS)
                }
            } else {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.SEND_SMS)
            }
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(
                Manifest.permission.READ_SMS)
        }
    }
}

@Composable
fun Conversations(threads: List<Thread>) {
    LazyColumn {
        items(threads) { thread ->
            val senderIds = ArrayList<Contact>();
            thread.recipients.split(" ").iterator().forEach {
                val contact = getContactById(it.toInt());
                if (contact != null) {
                    senderIds.add(contact);
                } else {
                    senderIds.add(Contact(it.toInt(), "🤷", null))
                }
            }
            var title = "";
            senderIds.forEach {
                title += it.displayName + ", ";
            }
            title.substring(0, title.lastIndex-2);
            var icon = null;
            senderIds.forEach { 
                if (it.pfp_uri != null) {
                    icon = painterResource(id = )
                }
            }
            ConversationPreview(title = title, lastMessage = Message("Chrissy", 1234, "I haven't quite figured this out yet :/"), icon = )
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
                text = lastMessage.author+": "+lastMessage.body,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview
@Composable
fun PreviewConversationPreview() {
    XMLSMSTheme {
        ConversationPreview(
            "Funny GC",
            Message("Miles", 1234, "Chrissyyyy"),
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
    XMLSMSTheme {
        ConversationMessages(ArrayList(listOf(Message("Lexi 😡😡😡", 1234, "I love android 🤓🤓🤓"))))
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
            val senderContact = getContactById(msg.sender_id)
            val authorDisplayName = senderContact?.displayName ?: msg.author
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
                                text = msg.body,
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
                            sendSMS(XMLSMS.createBody(messageInput), msg.author)
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
    XMLSMSTheme {
        Surface {
            MessageCard(
                msg = Message("Lexi", 1234, "Hey, take a look at Jetpack Compose, it's great!")
            )
        }
    }
}