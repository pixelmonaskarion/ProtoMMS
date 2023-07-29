package com.chrissytopher.pumpmessager.ui.conversation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.chrissytopher.pump.ProtoConstructors
import com.chrissytopher.pump.proto.PumpMessage
import com.chrissytopher.pumpmessager.Thread
import com.chrissytopher.pumpmessager.ui.theme.ProtoMMSTheme
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@Composable
fun ConversationMessages(messages: ArrayList<PumpMessage.Message>, thread: Thread) {
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
    }
}

@Preview
@Composable
fun PreviewConversationMessages() {
    ProtoMMSTheme {
        ConversationMessages(ArrayList(listOf(ProtoConstructors.Message("I love android 🤓🤓🤓", arrayOf("Lexi 😡😡😡"), arrayOf()))), Thread(0, ProtoConstructors.Message("", arrayOf()), ""))
    }
}