package com.example.chattingwithapi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingwithapi.databinding.MainActivityBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.*



class MainActivity : AppCompatActivity() {
    lateinit var btnAddchatRoom: Button
    lateinit var btnSignout: Button
    lateinit var btnCalender: Button
    lateinit var binding: MainActivityBinding
    lateinit var firebaseDatabase: DatabaseReference
    lateinit var recycler_chatroom: RecyclerView
    private val TAG: String = MainActivity::class.java.simpleName
    private val SERVER_URL = "ws://192.168.0.98:8000"
    private val SOCKET_PATH = "/fcm_server/"
    private var webSocket: WebSocket? = null

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.i(TAG, "Connected to server")
            // 연결 성공 시 수행할 작업
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            // 메시지 수신 시 동작할 내용을 작성합니다.
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.i(TAG, "Connection closing: $reason")
            // 연결 종료 전에 수행할 작업
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.i(TAG, "Disconnected from server")
            // 연결 종료 시 수행할 작업
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "Error connecting: ${t.message}")
            // 연결 오류 시 수행할 작업
        }
    }

    private fun connectToServer() {


        val client = OkHttpClient.Builder().build()

        val request = Request.Builder()
            .url(SERVER_URL + SOCKET_PATH)
            .build()

        webSocket = client.newWebSocket(request, webSocketListener)
        client.dispatcher.executorService.shutdown()
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocket?.cancel()
        webSocket = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeView()
        initializeListener()
        setupRecycler()
//        try {
//            val myFcmService = MyFcmService()
//            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val token = task.result
//                    if (token != null) {
//                        myFcmService.sendTokenToServer(token)
//                    } else {
//                        Log.e("MyFcmService", "Failed to get FCM token.")
//                    }
//                } else {
//                    Log.e(
//                        "MyFcmService",
//                        "Failed to get FCM token. Error: ${task.exception?.message}"
//                    )
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("MyFcmService", "Failed to get FCM token. Error: ${e.message}")
//        }
        connectToServer()
    }




    fun initializeView() { //뷰 초기화
        try {
            firebaseDatabase = FirebaseDatabase.getInstance().getReference("ChatRoom")
            btnSignout = binding.btnSignout
            btnAddchatRoom = binding.btnNewMessage
            btnCalender = binding.btnCalender
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
        btnCalender.setOnClickListener()
        {
            startActivity(Intent(this@MainActivity, CalenderActivity::class.java))
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
