package com.chrissytopher.pumpmessager

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.Telephony.Threads
import android.telephony.PhoneNumberUtils
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.chrissytopher.pump.ProtoConstructors
import com.chrissytopher.pump.proto.PumpMessage.*
import com.chrissytopher.pumpmessager.ui.conversation.ConversationMessages
import com.chrissytopher.pumpmessager.ui.theme.ProtoMMSTheme

class MainActivity : BaseActivity() {
    override fun startApp() {
        setContent {
            var conversation: Thread? by remember { mutableStateOf(null) }
            var screen: String by remember { mutableStateOf("home") }
            val conversations by remember { mutableStateOf(getThreads()) }
            ProtoMMSTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (screen == "home") {
                        Conversations(threads = conversations, {
                            conversation = it
                            screen = "messages"
                        }, {
                            conversation = null
                            screen = "new chat"
                        })
                    } else if (screen == "messages") {
                        ConversationMessages(messages = getThreadMessages(conversation!!), thread = conversation!!)
                    } else if (screen == "new chat") {
                        NewChat {
                            Log.d("ProtoMMS", "conv id: $it")
                            conversation = it
                            screen = "messages"
                        }
                    }
                }
            }
            BackHandler((screen != "home")) {
                conversation = null
                screen = "home"
            }
        }
    }
}



@Composable
@Preview
fun NewChatPreview() {
    NewChat {}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChat(startChat: (Thread) -> Unit) {
    var contactSearch by remember { mutableStateOf("") }
    var contacts = searchContacts(contactSearch)
    val context = LocalContext.current
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = contactSearch,
                onValueChange = {
                    contactSearch = it
                    contacts = searchContacts(contactSearch)
                },
                placeholder = {Text("Name or Phone Number")},
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    val displayNameContact = getContactByDisplayName(contactSearch)
                    if (displayNameContact != null) {
                        val threadId = Threads.getOrCreateThreadId(context, displayNameContact.phoneNumber)
                        startChat(Thread(threadId, getLastMessage(threadId), displayNameContact.phoneNumber!!))
                    } else {
                        if (PhoneNumberUtils.isGlobalPhoneNumber(PhoneNumberUtils.stripSeparators(contactSearch))) {
                            val threadId = Threads.getOrCreateThreadId(context, contactSearch)
                            startChat(Thread(threadId, getLastMessage(threadId), contactSearch))
                        }
                    }
                })
            )
            IconButton(onClick = {
                val displayNameContact = getContactByDisplayName(contactSearch)
                if (displayNameContact != null) {
                    val threadId = Threads.getOrCreateThreadId(context, displayNameContact.phoneNumber)
                    startChat(Thread(threadId, getLastMessage(threadId), displayNameContact.phoneNumber!!))
                } else {
                    if (PhoneNumberUtils.isGlobalPhoneNumber(PhoneNumberUtils.stripSeparators(contactSearch))) {
                        val threadId = Threads.getOrCreateThreadId(context, contactSearch)
                        startChat(Thread(threadId, getLastMessage(threadId), contactSearch))
                    }
                }
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Create Chat Button")
            }
        }

        LazyColumn() {
            items(contacts) {
                Row(modifier = Modifier
                    .padding(all = 8.dp)
                    .width(LocalConfiguration.current.screenWidthDp.dp)
                    .clickable {
                        val threadId = Threads.getOrCreateThreadId(context, it.phoneNumber)
                        startChat(Thread(threadId, getLastMessage(threadId), it.phoneNumber!!))
                    }) {
                    var icon = LocalContext.current.resourceUri(R.drawable.profile_picture)
                    if (it.pfp_uri != null) {
                        icon = Uri.parse(it.pfp_uri)
                    }
                    val painter: Painter = rememberAsyncImagePainter(icon)
                    Image(
                        painter = painter,
                        contentDescription = "Conversation profile Picture",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = it.displayName,
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
}

@Composable
@Preview
fun ConversationsPreview() {
    Conversations(threads = arrayListOf(), openThread = {}) {
        
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Conversations(threads: List<Thread>, openThread: (Thread) -> Unit, newChat: () -> Unit) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                newChat()
            }) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "New Chat Button"
                )
            }
        }
    ) {
        LazyColumn {
            items(threads) { thread ->
                var title = thread.address
                var icon = LocalContext.current.resourceUri(R.drawable.profile_picture)
                val contact = getContactByNumber(thread.address)
                if (contact != null) {
                    title = contact.displayName
                    if (contact.pfp_uri != null) {
                        icon = Uri.parse(contact.pfp_uri)
                    }
                }
                ConversationPreview(
                    title = title,
                    lastMessage = thread.lastMessage,
                    icon = icon,
                    openThread = {
                        openThread(thread)
                    })
            }
        }
    }
}

fun Context.resourceUri(resourceId: Int): Uri = with(resources) {
    Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(getResourcePackageName(resourceId))
        .appendPath(getResourceTypeName(resourceId))
        .appendPath(getResourceEntryName(resourceId))
        .build()
}

@Composable
fun ConversationPreview(title: String, lastMessage: Message?, icon: Uri, openThread: () -> Unit) {
    Row(modifier = Modifier
        .padding(all = 8.dp)
        .width(LocalConfiguration.current.screenWidthDp.dp)
        .clickable {
            openThread()
        }) {
        val painter: Painter = rememberAsyncImagePainter(icon)
        Image(
            painter = painter,
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
            var bodyText = ""
            if (lastMessage != null) {
                var senderName = lastMessage.sender.address
                if (lastMessage.sender.address != "") {
                    val contact = getContactByNumber(lastMessage.sender.address)
                    if (contact != null) {
                        senderName = contact.displayName
                    }
                    if (lastMessage.sender.address == getPhoneNumber()) {
                        senderName = "You"
                    }
                }
                bodyText = senderName+": " + lastMessage.text
            }
            Text(
                text = bodyText,
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
            ProtoConstructors.Message("Chrissyyyy", arrayOf("Miles")),
            LocalContext.current.resourceUri(R.drawable.profile_picture)
        ) {}
    }
}