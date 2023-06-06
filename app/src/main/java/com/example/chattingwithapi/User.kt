package com.example.chattingwithapi

import java.io.Serializable

data class User(val name:String?="",
                val uid:String?="",
                val email:String?="",):Serializable {

}