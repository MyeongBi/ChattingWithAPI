package com.example.chattingwithapi

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingwithapi.databinding.ListPersonItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RecyclerUsersAdapter(val context: Context, val onUserClickListener: OnUserClickListener) :
    RecyclerView.Adapter<RecyclerUsersAdapter.ViewHolder>() {
    var users: ArrayList<User> =arrayListOf()        //검색어로 일치한 사용자 목록
    val allUsers: ArrayList<User> =arrayListOf()    //전체 사용자 목록
    lateinit var currnentUser: User
    interface OnUserClickListener {
        fun onUserClick(user: User)
    }

    init {
        setupAllUserList()
    }

    fun setupAllUserList() {        //전체 사용자 목록 불러오기
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()        //현재 사용자 아이디
        FirebaseDatabase.getInstance().getReference("User").child("users")   //사용자 데이터 요청
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    users.clear()
                    for (data in snapshot.children) {
                        val item = data.getValue(User::class.java)
                        if (item?.uid.equals(myUid)) {
                            currnentUser = item!!             //전체 사용자 목록에서 현재 사용자는 제외
                            continue
                        }
                        allUsers.add(item!!)              //전체 사용자 목록에 추가
                    }
                    users = allUsers.clone() as ArrayList<User>
                    notifyDataSetChanged()              //화면 업데이트
                }
            })
    }

    fun searchItem(target: String) {            //검색
        if (target.equals("")) {      //검색어 없는 경우 전체 목록 표시
            users = allUsers.clone() as ArrayList<User>
        } else {
            var matchedList = allUsers.filter{ it.name!!.contains(target)}//검색어 포함된 항목 불러오기
            users.clear()
            matchedList.forEach{users.add(it)}
        }
        notifyDataSetChanged()          //화면 업데이트
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_person_item, parent, false)
        return ViewHolder(ListPersonItemBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txt_name.text = users[position].name
        holder.txt_email.text = users[position].email

        holder.background.setOnClickListener {
            if (context is AddChatRoomActivity) {
                addChatRoom(position) // AddChatRoomActivity에서 사용할 경우
            } else {
                onUserClickListener.onUserClick(users[position]) // DialogFragment에서 사용할 경우
            } //해당 사용자 선택 시
        }
    }


    fun addChatRoom(position: Int) {
        val opponent = users[position]
        val database = FirebaseDatabase.getInstance().getReference("ChatRoom")
        val chatRoom = ChatRoom(
            mapOf(currnentUser.uid!! to true, opponent.uid!! to true),
            null
        )
        val myUid = FirebaseAuth.getInstance().uid

        database.child("chatRooms")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    var existingChatRoom: ChatRoom? = null
                    var chatRoomId: String? = null
                    for (childSnapshot in snapshot.children) {
                        val roomId = childSnapshot.key as String
                        val room = childSnapshot.getValue(ChatRoom::class.java)
                        if (room != null && room.users.containsKey(myUid) && room.users.containsKey(opponent.uid)) {
                            existingChatRoom = room
                            chatRoomId = roomId
                            break
                        }
                    }
                    if (existingChatRoom != null && chatRoomId != null) {
                        goToChatRoom(existingChatRoom, opponent, chatRoomId)
                    } else {
                        createNewChatRoom(chatRoom, opponent)
                    }
                }
            })
    }

    private fun createNewChatRoom(chatRoom: ChatRoom, opponent: User) {
        val database = FirebaseDatabase.getInstance().getReference("ChatRoom")
        val newChatRoomRef = database.child("chatRooms").push()
        newChatRoomRef.setValue(chatRoom)
            .addOnSuccessListener {
                val chatRoomId = newChatRoomRef.key
                if (chatRoomId != null) {
                    goToChatRoom(chatRoom, opponent, chatRoomId)
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure to create a new chat room
            }
    }

    private fun goToChatRoom(chatRoom: ChatRoom, opponent: User, chatRoomId: String) {
        val intent = Intent(context, ChatRoomActivity::class.java)
        intent.putExtra("ChatRoom", chatRoom)
        intent.putExtra("Opponent", opponent)
        intent.putExtra("ChatRoomKey", chatRoomId)
        context.startActivity(intent)
        (context as AppCompatActivity).finish()
    }


    override fun getItemCount(): Int {
        return users.size
    }



    inner class ViewHolder(itemView: ListPersonItemBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var background = itemView.background
        var txt_name = itemView.txtName
        var txt_email = itemView.txtEmail

        init {
            background.isClickable = true
            background.setOnClickListener {
                if (context is AddChatRoomActivity) {
                    addChatRoom(adapterPosition)        // AddChatRoomActivity에서 사용할 경우
                } else if (context is DialogFragment) {
                    onUserClickListener.onUserClick(users[adapterPosition]) // DialogFragment에서 사용할 경우
                }        //해당 사용자 선택 시
            }
        }
    }

}
