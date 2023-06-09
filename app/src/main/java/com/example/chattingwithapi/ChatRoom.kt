package com.example.chattingwithapi

import android.os.Parcelable
import java.io.Serializable

data class ChatRoom(
    val users: Map<String?, Boolean> = HashMap(),
    var messages: Map<String,Message>? = HashMap()
) : Serializable {
}