package com.example.chattingwithapi

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.chattingwithapi.LoginActivity
import com.example.chattingwithapi.AddChatRoomActivity
import com.example.chattingwithapi.databinding.MainActivityBinding
import com.example.chattingwithapi.ChatRoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import org.jsoup.Connection
import org.jsoup.nodes.Element
import android.os.Message
import android.util.Log
import java.io.IOException


@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : AppCompatActivity() {
    lateinit var btnAddchatRoom: Button
    lateinit var btnSignout: Button
    lateinit var binding: MainActivityBinding
    lateinit var firebaseDatabase: DatabaseReference
    lateinit var recycler_chatroom: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeView()
        initializeListener()
        setupRecycler()

        val url = "https://lms.mju.ac.kr/ilos/main/main_form.acl"

        Thread {
            try {
                // 크롤링 작업 실행
                val divText = performLoginAndCrawling()


                // UI 업데이트 작업
                runOnUiThread {
                    if(divText != null)
                        Log.e("test", divText)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    fun performLoginAndCrawling(): String? {
        val loginUrl = "https://sso1.mju.ac.kr/login.do?redirect_uri=https://lms.mju.ac.kr/ilos/bandi/sso/index.jsp"
        val mainUrl = "https://lms.mju.ac.kr/ilos/main/main_form.acl"
        val username = ""
        val userpassword = ""

        try {
            // 로그인 폼 전송
            val loginResponse: Connection.Response = Jsoup.connect(loginUrl)
                .data("id", username)
                .data("passwrd", userpassword)
                .method(Connection.Method.POST)
                .execute()
          //  Log.e("connectioncode", loginResponse.statusCode().toString())
            val cookies = loginResponse.cookies()

            // redirect 된 페이지에 접속하여 크롤링 또는 스크래핑 작업 수행
            val mainPageResponse: Connection.Response = Jsoup.connect(mainUrl)
                .cookies(cookies)
                .execute()
           // Log.e("mainpageconnectioncode", mainPageResponse.statusCode().toString())
            val mainPageDoc: Document = mainPageResponse.parse()

            // 추가적인 크롤링 또는 스크래핑 작업 수행
            val divText = mainPageDoc.selectFirst("div.m-box2 ol li em.sub_open")?.text() // 예시: 특정 div 요소의 텍스트 가져오기

            // 결과 출력
            if (divText != null) {
                return divText
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }











    fun initializeView() { //뷰 초기화
        try {
            firebaseDatabase = FirebaseDatabase.getInstance().getReference("ChatRoom")!!
            btnSignout = binding.btnSignout
            btnAddchatRoom = binding.btnNewMessage
            recycler_chatroom = binding.recyclerChatrooms

            val handler = object : Handler(Looper.getMainLooper()) {
                override fun handleMessage(msg: Message) {
                    val bundle = msg.data // new Thread에서 작업한 결과물 받기
                    bundle.getString("message")?.let { Log.e("testString", it) }
                }
            }


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