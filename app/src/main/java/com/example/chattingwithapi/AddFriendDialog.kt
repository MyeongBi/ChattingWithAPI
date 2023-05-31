package com.example.chattingwithapi

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
    private lateinit var findPeople: EditText
    private lateinit var btnExit: Button
    private lateinit var firebaseDatabase: DatabaseReference


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

    private fun setupRecycler() {
        recyclerPeople.layoutManager = LinearLayoutManager(requireContext())
        recyclerPeople.adapter = RecyclerUsersAdapter(requireContext(), object : RecyclerUsersAdapter.OnUserClickListener {
            override fun onUserClick(user: User) {
                addUserToChatRoom(user)
                dismiss()
            }
        })

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
                    Log.e("AddFriendDialogFragment", "User added to chat room: ${user.uid}")
                }
                .addOnFailureListener { error ->
                    // 사용자 추가 실패 처리
                    Log.e("AddFriendDialogFragment", "Failed to add user to chat room: ${user.uid}, error: $error")
                }
        }
    }




}


