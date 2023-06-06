package com.example.chattingwithapi

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingwithapi.databinding.AddfriendActivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddFriendDialogFragment(private val chatRoomId: String): DialogFragment() {
    private lateinit var binding: AddfriendActivityBinding
    private lateinit var recyclerPeople: RecyclerView
    private lateinit var recyclerFriends: RecyclerView
    private lateinit var findPeople: EditText
    private lateinit var btnExit: Button
    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var fragmentContext: Context

    constructor(chatRoomId: String, otherParameter: String) : this(chatRoomId) {
        // 다른 파라미터에 대한 추가적인 초기화 작업 수행
    }

    // 생성자 2: chatRoomId 외에 다른 인자를 받지 않는 생성자
    constructor() : this("defaultChatRoomId") {
        // chatRoomId를 기본값으로 초기화하고, 다른 파라미터에 대한 추가적인 초기화 작업 수행
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.addfriend_activity, container, false)
        binding = AddfriendActivityBinding.bind(view)
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        initializeListener()
        setupRecycler()
    }

    private fun initializeView() {
        firebaseDatabase = FirebaseDatabase.getInstance().reference
        btnExit = binding.btnBack
        findPeople = binding.edtOpponentName2
        recyclerPeople = binding.recyclerFriends
    }

    private fun initializeListener() {
        btnExit.setOnClickListener {
            dismiss()
        }

        findPeople.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val adapter = recyclerPeople.adapter as? RecyclerUsersAdapter
                adapter?.searchItem(s.toString())
            }
        })
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    private fun setupRecycler() {
        recyclerPeople.layoutManager = LinearLayoutManager(requireContext())
        recyclerPeople.adapter = RecyclerUsersAdapter(requireContext(), object : RecyclerUsersAdapter.OnUserClickListener {
            override fun onUserClick(user: User) {
                if (chatRoomId == "defaultChatRoomId") {
                    addFriend(user)
                } else {
                    addUserToChatRoom(user)
                }
                dismiss()
            }
        })

    }

    private fun addFriend(user: User) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid
        val friendUid = user.uid


        if (fragmentContext != null && myUid != null && friendUid != null) {
            val currentUserRef = FirebaseDatabase.getInstance().getReference("User")
                .child("users")
                .child(myUid)
            val friendRef = FirebaseDatabase.getInstance().getReference("User")
                .child("users")
                .child(friendUid)

            // 내 정보에서 친구 UID 추가
            currentUserRef.child("friends").child(friendUid).setValue(true)
                .addOnSuccessListener {
                    // 친구 추가 성공
                    Toast.makeText(fragmentContext, "친구 추가 성공", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { error ->
                    // 친구 추가 실패
                    Toast.makeText(fragmentContext, "친구 추가 실패", Toast.LENGTH_SHORT).show()
                }

            // 친구 정보에서 내 UID 추가
            friendRef.child("friends").child(myUid).setValue(true)
                .addOnSuccessListener {
                    // 친구 추가 성공
                    // 여기서 필요한 UI 업데이트를 수행하거나 사용자에게 알림을 표시할 수 있습니다.
                }
                .addOnFailureListener { error ->
                    // 친구 추가 실패
                    // 실패 처리에 대한 로직을 추가할 수 있습니다.
                }
        }
    }




    private fun addUserToChatRoom(user: User) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid
        val chatRoomRef = FirebaseDatabase.getInstance().getReference("ChatRoom").child("chatRooms").child(chatRoomId)

        val chatRoomUserRef = chatRoomRef.child("users")
        val userRef = user.uid?.let { chatRoomUserRef.child(it) }

        if (userRef != null) {
            userRef.setValue(true)
                .addOnSuccessListener {
                    // 사용자가 채팅방에 추가되었음을 사용자에게 표시하거나 필요한 UI 업데이트를 수행하세요
                    // 예를 들어, 채팅방에 사용자가 추가되었음을 Toast 메시지로 표시
                    // 또는 채팅방에 사용자가 추가되었음을 알리는 알림을 표시
                   // Toast.makeText(requireContext(), "User added to chat room", Toast.LENGTH_SHORT).show()
                    Log.e("AddFriendDialogFragment", "User added to chat room: ${user.uid}")
                }
                .addOnFailureListener { error ->
                    // 사용자 추가 실패 처리
                    Log.e("AddFriendDialogFragment", "Failed to add user to chat room: ${user.uid}, error: $error")
                }
        }
    }




}


