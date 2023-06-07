package com.example.chattingwithapi

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingwithapi.databinding.ListChatroomItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.O)
class RecyclerChatRoomsAdapter(val context: Context) :
    RecyclerView.Adapter<RecyclerChatRoomsAdapter.ViewHolder>() {
    var chatRooms: ArrayList<ChatRoom> = arrayListOf()   //채팅방 목록
    var chatRoomKeys: ArrayList<String> = arrayListOf()  //채팅방 키 목록
    val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()   //현재 사용자 Uid

    init {
        setupAllUserList()
    }

    fun setupAllUserList() {     //전체 채팅방 목록 초기화 및 업데이트
        FirebaseDatabase.getInstance().getReference("ChatRoom").child("chatRooms")
            .orderByChild("users/$myUid").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {//채팅방 목록 불러오기
                    chatRooms.clear()
                    for (data in snapshot.children) {
                        val chatRoom = data.getValue(ChatRoom::class.java) as ChatRoom
                        chatRooms.add(chatRoom)
                        chatRoomKeys.add(data.key!!)
                    }

                    notifyDataSetChanged()
                }
            })

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_chatroom_item, parent, false)
        return ViewHolder(ListChatroomItemBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userIdList = chatRooms[position].users!!.keys    // 채팅방에 포함된 사용자 키 목록
        val myUid = myUid // 여기에 자신의 UID를 입력해주세요
        val opponentUids = userIdList.filter { it != myUid }  // 상대방 사용자 키들

        FirebaseDatabase.getInstance().getReference("User").child("users")
            .orderByChild("uid")
            .equalTo(opponentUids[0])
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        holder.chatRoomKey = data.key.toString()  // 채팅방 키 초기화
                        holder.opponentUser = data.getValue(User::class.java)!!  // 상대방 정보 초기화
                        holder.txt_name.text = data.getValue(User::class.java)?.name.toString()  // 상대방 이름 초기화

                        // 채팅방에 참여하는 사용자들의 이름 표시
                        if (opponentUids.size > 1) {
                            holder.txt_name.append(", ...")  // 3명 이상인 경우 "..." 추가

                            for (i in 1 until minOf(opponentUids.size, 4)) {
                                val uid = opponentUids[i]
                                FirebaseDatabase.getInstance().getReference("User").child("users")
                                    .orderByChild("uid")
                                    .equalTo(uid)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onCancelled(error: DatabaseError) {}
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for (data in snapshot.children) {
                                                val username = data.getValue(User::class.java)?.name.toString()
                                                holder.txt_name.append(", $username")
                                            }
                                        }
                                    })
                            }
                        }
                    }
                }
            })

        holder.background.setOnClickListener() {               // 채팅방 항목 선택 시
            try {
                val intent = Intent(context, ChatRoomActivity::class.java)
                intent.putExtra("ChatRoom", chatRooms[position])  // 채팅방 정보
                intent.putExtra("Opponent", holder.opponentUser)  // 상대방 사용자 정보
                intent.putExtra("ChatRoomKey", chatRoomKeys[position])  // 채팅방 키 정보
                context.startActivity(intent)  // 해당 채팅방으로 이동
                (context as AppCompatActivity).finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "채팅방 이동 중 문제가 발생하였습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        if (chatRooms[position].messages!!.isNotEmpty()) {  // 채팅방 메시지가 존재하는 경우
            setupLastMessageAndDate(holder, position)  // 마지막 메시지 및 시각 초기화
            setupMessageCount(holder, position)
        }
    }


    fun setupLastMessageAndDate(holder: ViewHolder, position: Int) { //마지막 메시지 및 시각 초기화
        try {
            var lastMessage =
                chatRooms[position].messages!!.values.sortedWith(compareBy({ it.sended_date }))    //메시지 목록에서 시각을 비교하여 가장 마지막 메시지  가져오기
                    .last()
            holder.txt_message.text = lastMessage.content                 //마지막 메시지 표시
            holder.txt_date.text = getLastMessageTimeString(lastMessage.sended_date)   //마지막으로 전송된 시각 표시
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setupMessageCount(holder: ViewHolder, position: Int) {            //확인되지 않은 메시지 개수 표시
        try {
            var unconfirmedCount =
                chatRooms[position].messages!!.filter {
                    !it.value.confirmed && !it.value.senderUid.equals(               //메시지 중 확인되지 않은 메시지 개수 가져오기
                        myUid
                    )
                }.size
            if (unconfirmedCount > 0) {              //확인되지 않은 메시지가 있을 경우
                holder.txt_chatCount.visibility = View.VISIBLE           //개수 표시
                holder.txt_chatCount.text = unconfirmedCount.toString()
            } else
                holder.txt_chatCount.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
            holder.txt_chatCount.visibility = View.GONE
        }
    }

    fun getLastMessageTimeString(lastTimeString: String): String {
        try {
            val currentTime = LocalDateTime.now()
            val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

            val lastMessageTime = LocalDateTime.parse(lastTimeString, dateTimeFormatter)

            val monthsAgo = ChronoUnit.MONTHS.between(lastMessageTime, currentTime)
            val daysAgo = ChronoUnit.DAYS.between(lastMessageTime, currentTime)
            val hoursAgo = ChronoUnit.HOURS.between(lastMessageTime, currentTime)
            val minutesAgo = ChronoUnit.MINUTES.between(lastMessageTime, currentTime)

            return when {
                monthsAgo > 0 -> "$monthsAgo 개월 전"
                daysAgo > 0 -> if (daysAgo.toInt() == 1
                ) "어제" else "$daysAgo 일 전"
                hoursAgo > 0 -> "$hoursAgo 시간 전"
                minutesAgo > 0 -> "$minutesAgo 분 전"
                else -> "방금"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }


    override fun getItemCount(): Int {
        return chatRooms.size
    }

    inner class ViewHolder(itemView: ListChatroomItemBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var opponentUser = User("", "")
        var chatRoomKey = ""
        var background = itemView.background
        var txt_name = itemView.txtName
        var txt_message = itemView.txtMessage
        var txt_date = itemView.txtMessageDate
        var txt_chatCount = itemView.txtChatCount
    }

}