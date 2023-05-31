package com.example.chattingwithapi

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingwithapi.databinding.ChatroomActivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.w3c.dom.Text
import android.widget.Toast
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URISyntaxException

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class ChatRoomActivity : AppCompatActivity() {
    lateinit var binding: ChatroomActivityBinding
    lateinit var btn_exit: ImageButton
    lateinit var btn_submit: Button
    lateinit var txt_title: TextView
    lateinit var edt_message: EditText
    lateinit var firebaseDatabase: DatabaseReference
    lateinit var recycler_talks: RecyclerView
    lateinit var chatRoom: ChatRoom
    lateinit var opponentUser: User
    lateinit var chatRoomKey: String
    lateinit var myUid: String
    lateinit var btn_notice: Button
    lateinit var recycler_people: RecyclerView
    lateinit var find_people: EditText
    private lateinit var menuLayout: LinearLayout
    var notice: TextView? = null
    private var isMenuVisible = false


    //var url = "http://172.20.10.11:5000/crawl"
    //private val SERVER_URL = "http://172.20.10.11:5000"
    //private val TAG = "ChatRoomActivity"

   // private lateinit var socket: Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ChatroomActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeProperty()
        initializeView()
        initializeListener()
        setupChatRooms()
    }

    fun initializeProperty() {  //변수 초기화
        myUid = FirebaseAuth.getInstance().currentUser?.uid!!              //현재 로그인한 유저 id
        firebaseDatabase = FirebaseDatabase.getInstance().reference!!

        chatRoom = (intent.getSerializableExtra("ChatRoom")) as ChatRoom      //채팅방 정보
        chatRoomKey = intent.getStringExtra("ChatRoomKey")!!            //채팅방 키
        opponentUser = (intent.getSerializableExtra("Opponent")) as User    //상대방 유저 정보
    }

    fun initializeView() {    //뷰 초기화
        btn_exit = binding.QuitBtn
        edt_message = binding.edtMessage
        recycler_talks = binding.recyclerMessages
        btn_submit = binding.btnSubmit
        txt_title = binding.txtTItle
        txt_title.text = opponentUser!!.name ?: ""
        btn_notice = binding.getnotice
        recycler_people = binding.recyclerPeoples
        find_people = binding.edtfind
    }

    fun initializeListener() {   //버튼 클릭 시 리스너 초기화
        btn_exit.setOnClickListener()
        {
            startActivity(Intent(this@ChatRoomActivity, MainActivity::class.java))
        }
        btn_submit.setOnClickListener()
        {
            putMessage()
        }
        btn_notice.setOnClickListener(){
            toggleMenuVisibility()
        }
        find_people.addTextChangedListener(object :
            TextWatcher                  //검색 창에 입력된 글자가 변경될 때마다 검색 내용 업데이트
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                var adapter = recycler_people.adapter as RecyclerUsersAdapter
                adapter.searchItem(s.toString())                  //입력된 검색어로 검색 진행 및 업데이트
            }
        })
    }
    private fun toggleMenuVisibility() {
        if (isMenuVisible) {
            hideMenu()
        } else {
            showMenu()
        }
    }

    private fun showMenu() {
        menuLayout.visibility = View.VISIBLE
        isMenuVisible = true
    }

    private fun hideMenu() {
        menuLayout.visibility = View.GONE
        isMenuVisible = false
    }

    fun setupChatRooms() {              //채팅방 목록 초기화 및 표시
        if (chatRoomKey.isNullOrBlank())
            setupChatRoomKey()
        else
            setupRecycler()
    }

    fun setupChatRoomKey() {            //chatRoomKey 없을 경우 초기화 후 목록 초기화
        FirebaseDatabase.getInstance().getReference("ChatRoom")
            .child("chatRooms").orderByChild("users/${opponentUser.uid}").equalTo(true)    //상대방의 Uid가 포함된 목록이 있는지 확인
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        chatRoomKey = (data.key).toString()         //chatRoomKey 초기화
                        setupRecycler()                  //목록 업데이트
                        break
                    }
                }
            })
    }

    fun putMessage() {       //메시지 전송
        try {
            var message = Message(myUid, getDateTimeString(), edt_message.text.toString())    //메시지 정보 초기화
            Log.i("ChatRoomKey", chatRoomKey)
            FirebaseDatabase.getInstance().getReference("ChatRoom").child("chatRooms")
                .child(chatRoomKey).child("messages")                   //현재 채팅방에 메시지 추가
                .push().setValue(message).addOnSuccessListener {
                    Log.i("putMessage", "메시지 전송에 성공하였습니다.")
                    edt_message.text.clear()
                }.addOnCanceledListener {
                    Log.i("putMessage", "메시지 전송에 실패하였습니다")
                }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("putMessage", "메시지 전송 중 오류가 발생하였습니다.")
        }
    }


    fun getDateTimeString(): String {          //메시지 보낸 시각 정보 반환
        try {
            var localDateTime = LocalDateTime.now()
            localDateTime.atZone(TimeZone.getDefault().toZoneId())
            var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            return localDateTime.format(dateTimeFormatter).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("getTimeError")
        }
    }

    fun setupRecycler() {            //목록 초기화 및 업데이트
        recycler_talks.layoutManager = LinearLayoutManager(this)
        recycler_talks.adapter = RecyclerMessagesAdapter(this, chatRoomKey, opponentUser.uid)

        recycler_people.layoutManager = LinearLayoutManager(this)
        recycler_people.adapter = RecyclerUsersAdapter(this)
    }

//    override fun onDataReceived(divText1: String?, divText2: String?) {
//        // divText1, divText2 변수에 서버에서 받은 데이터가 들어옵니다.
//        // 필요한 로직을 구현하여 데이터를 처리해주세요.
//        // 예를 들어, 텍스트를 TextView에 설정하는 경우:
//        Log.e("testString", divText1.toString())
//        Log.e("testString", divText2.toString())
//        runOnUiThread {
//            notice?.text = divText1
//        }
//    }
}