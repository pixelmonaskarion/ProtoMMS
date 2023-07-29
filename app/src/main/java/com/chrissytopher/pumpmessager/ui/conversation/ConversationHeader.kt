package com.chrissytopher.pumpmessager.ui.conversation

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.chrissytopher.pump.ProtoConstructors.Address
import com.chrissytopher.pump.proto.PumpMessage.Address
import com.chrissytopher.pumpmessager.R
import com.chrissytopher.pumpmessager.getContactByNumber
import com.chrissytopher.pumpmessager.resourceUri
import com.chrissytopher.pumpmessager.ui.theme.ProtoMMSTheme

@Composable
fun ConversationHeader(address: Address, forcePfp: Boolean) {
    var icon: Uri? = null
    if (forcePfp) {
        icon = LocalContext.current.resourceUri(R.drawable.profile_picture)
    }
    var displayName = address.address
    //Usually shouldn't happen but legacy problems 🤷
    val contact = if (address.address != "") getContactByNumber(address.address) else null
    if (contact != null) {
        displayName = contact.displayName
        if (contact.pfp_uri != null) {
            icon = Uri.parse(contact.pfp_uri)
        }
    }
    Surface(
        Modifier
            .fillMaxWidth()
            .height(60.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            icon?.let { Image(painter = rememberAsyncImagePainter(model = icon), contentDescription = "Contact Profile Picture", modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(displayName, style = MaterialTheme.typography.titleLarge)
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
fun ConversationHeaderPreview() {
    ProtoMMSTheme {
        Surface {
            ConversationHeader(Address("+1234567890"), true)
        }
    }
}