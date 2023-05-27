package io.github.pixelmonaskarion.protomms

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony.Threads
import android.telephony.PhoneNumberUtils
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import io.github.pixelmonaskarion.protomms.proto.ProtoMms
import io.github.pixelmonaskarion.protomms.proto.ProtoMms.Message
import io.github.pixelmonaskarion.protomms.ui.theme.ProtoMMSTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    val startApp = {
        setContent {
            var conversation: Long? by remember { mutableStateOf(null) }
            var screen: String by remember { mutableStateOf("home") }
            var conversations by remember { mutableStateOf(getThreads()) }
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
                        ConversationMessages(messages = getThreadMessages(conversation!!), conversation = conversation!!)
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
        val permissions = arrayListOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_NUMBERS)
        var allPermissionsAllowed = true
        for (permission in permissions) {
            if (!hasPermission(applicationContext, permission)) {
                allPermissionsAllowed = false
            }
        }
        if (allPermissionsAllowed) {
            startApp()
        } else {
            setContent {
                Column() {
                    Text(text = "Please allow this app the following permissions:")
                    Text(text = "Read and send SMS messages, this enables app functionality and is used solely inside the app.")
                    Text(text = "View phone contacts, this is used for the purpose of displaying contact names and profile pictures inside the app")
                    Text(text = "View the phone number of the current device, this allows the app to send PCS messages correctly")
                    Button(onClick = {
                        checkPermissions(permissions, applicationContext)
                    }) {
                        Text(text = "Continue")
                    }
                }
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
fun NewChat(startChat: (Long) -> Unit) {
    var contactSearch by remember { mutableStateOf("") }
    var contacts = searchContacts(contactSearch)
    val context = LocalContext.current
    Column() {
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
                        startChat(Threads.getOrCreateThreadId(context, displayNameContact.phoneNumber))
                    } else {
                        if (PhoneNumberUtils.isGlobalPhoneNumber(PhoneNumberUtils.stripSeparators(contactSearch))) {
                            startChat(Threads.getOrCreateThreadId(context, contactSearch))
                        }
                    }
                })
            )
            IconButton(onClick = {
                val displayNameContact = getContactByDisplayName(contactSearch)
                if (displayNameContact != null) {
                    startChat(Threads.getOrCreateThreadId(context, displayNameContact.phoneNumber))
                } else {
                    if (PhoneNumberUtils.isGlobalPhoneNumber(PhoneNumberUtils.stripSeparators(contactSearch))) {
                        startChat(Threads.getOrCreateThreadId(context, contactSearch))
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
                        startChat(Threads.getOrCreateThreadId(context, it.phoneNumber))
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
fun Conversations(threads: List<Thread>, openThread: (Long) -> Unit, newChat: () -> Unit) {
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
                        openThread(thread.id)
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
                    val contact = getContactByNumber(lastMessage.sender.address);
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
            Message("Chrissyyyy", arrayOf(Address("Miles")), arrayOf()),
            LocalContext.current.resourceUri(R.drawable.profile_picture)
        ) {}
    }
}

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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@Composable
fun ConversationMessages(messages: ArrayList<Message>, conversation: Long) {
    val lazyColumnListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    if (messages.size > 0) {
        coroutineScope.launch {
            lazyColumnListState.scrollToItem(messages.size - 1)
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = lazyColumnListState,
            modifier = Modifier.weight(1f)
        ) {
            items(messages) { message ->
                MessageCard(message)
            }
        }
        Box(
            modifier = Modifier.wrapContentSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            MessageInputBox { body, attachmentUri ->
                var attachments: Array<ProtoMms.Attachment> = arrayOf();
                if (attachmentUri != null) {
                    attachments = arrayOf(Attachment(attachmentUri))
                }
                sendMessage(
                    Message(
                        body,
                        arrayOf(Address(getThread(conversation)!!.address)),
                        attachments
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewConversationMessages() {
    ProtoMMSTheme {
        ConversationMessages(ArrayList(listOf(Message("I love android 🤓🤓🤓", arrayOf(Address("Lexi 😡😡😡")), arrayOf()))), 0)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageCard(msg: Message) {
    var displayName = msg.sender.address
    var icon = LocalContext.current.resourceUri(R.drawable.profile_picture)
    //Usually shouldn't happen but legacy problems 🤷
    val contact = if (msg.sender.address != "") getContactByNumber(msg.sender.address) else null
    if (contact != null) {
        displayName = contact.displayName
        if (contact.pfp_uri != null) {
            icon = Uri.parse(contact.pfp_uri)
        }
    }
    Row(modifier = Modifier
        .padding(all = 8.dp)) {
        Image(
            painter = rememberAsyncImagePainter(model = icon),
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
            Text(
                text = displayName,
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
                            sendMessage(Message(messageInput, arrayOf(Address(msg.sender.address)), arrayOf()))
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
                msg = Message("Hey, take a look at Jetpack Compose, it's great!", arrayOf(Address("Lexi")), arrayOf())
            )
        }
    }
}