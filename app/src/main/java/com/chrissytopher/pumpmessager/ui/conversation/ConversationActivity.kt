package com.chrissytopher.pumpmessager.ui.conversation

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toFile
import com.chrissytopher.pump.ProtoConstructors.Address
import com.chrissytopher.pump.ServerUtils
import com.chrissytopher.pump.proto.PumpMessage
import com.chrissytopher.pumpmessager.BaseActivity
import com.chrissytopher.pumpmessager.Rfc5724Uri
import com.chrissytopher.pumpmessager.Thread
import com.chrissytopher.pumpmessager.getThreadId
import com.chrissytopher.pumpmessager.getThreadMessages
import com.chrissytopher.pumpmessager.sendMessageCompat
import com.chrissytopher.pumpmessager.ui.theme.ProtoMMSTheme
import kotlinx.coroutines.runBlocking
import java.net.URISyntaxException

class ConversationActivity : BaseActivity() {
    private var address: String? = null
    private var text: String? = null
    override fun startApp() {
        val conversation = address?.let { Thread(getThreadId(it), null, it) }
        setContent {
            ProtoMMSTheme {
                Surface {
                    ConversationScreen(conversation)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ProtoMMS", "intent: $intent")
        if (intent.action.equals(Intent.ACTION_SENDTO)) {
            address = intent.data?.schemeSpecificPart
            text = intent.getStringExtra("sms_body")
        } else if (intent.data != null && intent.data?.scheme == "content") {
            var cursor: Cursor? = null
            try {
                cursor = contentResolver.query(intent.data!!, null, null, null, null)
                if (cursor != null && cursor.moveToNext()) {
                    address = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                            ContactsContract.RawContacts.Data.DATA1
                        )
                    )
                }
            } finally {
                cursor?.close()
            }
        } else {
            try {
                val smsUri = Rfc5724Uri(intent.data.toString())
                address = smsUri.path
                text = smsUri.queryParams["body"]
            } catch (e: URISyntaxException) {
                Log.w("ProtoMMS", "unable to parse RFC5724 URI from intent", e)
            }
        }
        address = PhoneNumberUtils.formatNumber(address, "+1")
        Log.d("ProtoMMS", "number: $address text: $text")
    }
}

@Composable
fun ConversationScreen(conversation: Thread?) {

    Box(modifier = Modifier.fillMaxSize()) {
        conversation?.let {
            Box(modifier = Modifier.align(Alignment.TopCenter)) {
                ConversationHeader(address = Address(conversation.address), forcePfp = false)
            }
            ConversationMessages(messages =  getThreadMessages(it), thread = it)
            Box(
                modifier = Modifier.wrapContentSize().align(Alignment.BottomCenter),
            ) {
                MessageInputBox { body, attachmentUri ->
                    var attachments: List<PumpMessage.Attachment> = listOf()
                    if (attachmentUri != null) {
                        attachments = listOf(ServerUtils.PostAttachment("pcs.chrissytopher.com", attachmentUri.toFile()))
                    }
                    runBlocking {
                        sendMessageCompat(
                            body,
                            listOf(it.address),
                            null,
                            attachments
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ConversationScreenPreview() {
    ProtoMMSTheme {
        Surface {
            ConversationScreen(conversation = Thread(0, null,"123"))
        }
    }
}