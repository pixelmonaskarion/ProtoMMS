package com.chrissytopher.pumpmessager.ui.conversation

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.chrissytopher.pump.ProtoConstructors
import com.chrissytopher.pump.proto.PumpMessage
import com.chrissytopher.pumpmessager.R
import com.chrissytopher.pumpmessager.getContactByNumber
import com.chrissytopher.pumpmessager.resourceUri
import com.chrissytopher.pumpmessager.ui.theme.ProtoMMSTheme
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageCard(msg: PumpMessage.Message) {
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
                            runBlocking {
//                                sendMessage(Message(messageInput, arrayOf(Address(msg.sender.address)), arrayOf()))
                            }
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
                msg = ProtoConstructors.Message("Hey, take a look at Jetpack Compose, it's great!", arrayOf("Lexi"))
            )
        }
    }
}