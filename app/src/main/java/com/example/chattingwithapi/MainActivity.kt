package com.example.chattingwithapi

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingwithapi.databinding.MainActivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import java.net.URISyntaxException


@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {
    lateinit var btnAddchatRoom: Button
    lateinit var btnSignout: Button
    lateinit var binding: MainActivityBinding
    lateinit var firebaseDatabase: DatabaseReference
    lateinit var recycler_chatroom: RecyclerView
    private var socket: Socket? = null
    private val TAG: String = MainActivity::class.java.simpleName
    private val SERVER_URL = "http://220.121.88.61:8000/"

    private fun connectToServer() {
        try {
            val myFcmService = MyFcmService()
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    if (token != null) {
                        myFcmService.sendTokenToServer(token)
                    } else {
                        Log.e("MyFcmService", "Failed to get FCM token.")
                    }
                } else {
                    Log.e("MyFcmService", "Failed to get FCM token. Error: ${task.exception?.message}")
                }
            }

            val options = IO.Options() // 옵션 설정
            options.transports= arrayOf("websocket")
            options.forceNew = true // 새로운 연결로 설정
            val socket = IO.socket(SERVER_URL, options)

            socket.on(Socket.EVENT_CONNECT) { args ->
                runOnUiThread {
                    Log.i(TAG, "connected")
                    // 연결 성공 시 수행할 작업
                    socket.emit("start_monitoring")
                }
            }


            socket.on(Socket.EVENT_DISCONNECT) { args ->
                runOnUiThread {
                    Log.i(TAG, "disconnected")
                    // 연결 종료 시 수행할 작업
                }
            }

            socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
                runOnUiThread {
                    Log.e(TAG, "Error connecting: 서버와 연결에 실패했습니다.")
                    // 연결 오류 시 수행할 작업
                }
            }

            socket.connect()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        socket?.disconnect()
        socket?.off(Socket.EVENT_CONNECT, onConnect)
        socket?.off(Socket.EVENT_DISCONNECT, onDisconnect)
        socket?.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
    }

    private val onConnect =
        Emitter.Listener { args: Array<Any?>? ->
            runOnUiThread {
                Log.i(
                    TAG,
                    "Connected to server"
                )
            }
        }
    private val onDisconnect =
        Emitter.Listener { args: Array<Any?>? ->
            runOnUiThread {
                Log.i(
                    TAG,
                    "Disconnected from server"
                )
            }
        }
    private val onConnectError =
        Emitter.Listener { args: Array<Any?>? ->
            runOnUiThread {
                Log.e(
                    TAG,
                    "Connection error"
                )
            }
        }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val SERVER_URL = "http://220.121.88.61:8000" // 수정: 서버 URL 입력
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeView()
        initializeListener()
        setupRecycler()
        val myFcmService = MyFcmService()
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                if (token != null) {
                    myFcmService.sendTokenToServer(token)
                } else {
                    Log.e("MyFcmService", "Failed to get FCM token.")
                }
            } else {
                Log.e("MyFcmService", "Failed to get FCM token. Error: ${task.exception?.message}")
            }
        }
      //  connectToServer()

    }




    fun initializeView() { //뷰 초기화
        try {
            firebaseDatabase = FirebaseDatabase.getInstance().getReference("ChatRoom")!!
            btnSignout = binding.btnSignout
            btnAddchatRoom = binding.btnNewMessage
            recycler_chatroom = binding.recyclerChatrooms

        }catch (e:Exception)
        {
            e.printStackTrace()
            Toast.makeText(this,"화면 초기화 중 오류가 발생하였습니다.",Toast.LENGTH_LONG).show()
        }
    }
    fun initializeListener()  //버튼 클릭 시 리스너 초기화
    {
        btnSignout.setOnClickListener()
        {
            signOut()
        }
        btnAddchatRoom.setOnClickListener()  //새 메시지 화면으로 이동
        {
            startActivity(Intent(this@MainActivity, AddChatRoomActivity::class.java))
            finish()
        }
    }

    fun setupRecycler() {
        recycler_chatroom.layoutManager = LinearLayoutManager(this)
        recycler_chatroom.adapter = RecyclerChatRoomsAdapter(this)
    }

    fun signOut()    //로그아웃 실행
    {
        try {

            val builder = AlertDialog.Builder(this)
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("확인"
                ) { dialog, id ->
                    try {
                        FirebaseAuth.getInstance().signOut()             //로그아웃
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        dialog.dismiss()
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        dialog.dismiss()
                        Toast.makeText(this, "로그아웃 중 오류가 발생하였습니다.", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("취소"          //다이얼로그 닫기
                ) { dialog, id ->
                    dialog.dismiss()
                }
            builder.show()
        }catch (e:Exception)
        {
            e.printStackTrace()
            Toast.makeText(this,"로그아웃 중 오류가 발생하였습니다.",Toast.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed() {

        signOut()
    }

}
