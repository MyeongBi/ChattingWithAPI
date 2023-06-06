package com.example.chattingwithapi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingwithapi.databinding.AddfriendActivityBinding
import com.example.chattingwithapi.databinding.RemoveFriendActivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RemoveFriendFragment(private val friendId: String) : DialogFragment() {
    private lateinit var binding: RemoveFriendActivityBinding
    private lateinit var adapter: RecyclerFriendsAdapter
    private lateinit var recyclerFriends: RecyclerView
    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var btnback: Button
    private val friendList: ArrayList<User> = ArrayList()

    constructor() : this("")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.remove_friend_activity, container, false)
        binding = RemoveFriendActivityBinding.bind(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        initializeListener()
       // fetchFriendList()
        setupRecycler()
    }

    private fun initializeView() {
        firebaseDatabase = FirebaseDatabase.getInstance().reference
        btnback = binding.btnBack
        recyclerFriends = binding.recyclerFriends
    }

    private fun setupRecycler() {
        recyclerFriends.layoutManager = LinearLayoutManager(requireContext())
        val adapter = RecyclerFriendsAdapter(requireContext()) { user ->
            user.uid?.let { removeFriend(it) }
        }
        recyclerFriends.adapter = adapter
    }


    private fun initializeListener() {
        btnback.setOnClickListener {
            dismiss()
        }
    }

//    private fun fetchFriendList() {
//        val myUid = FirebaseAuth.getInstance().currentUser?.uid
//        val friendRef = myUid?.let {
//            FirebaseDatabase.getInstance().getReference("User")
//                .child("users")
//                .child(it)
//                .child("friends")
//        }
//
//        friendRef?.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                friendList.clear()
//                for (friendSnapshot in snapshot.children) {
//                    val friend = friendSnapshot.getValue(User::class.java)
//                    friend?.let {
//                        friendList.add(it)
//                    }
//                }
//                adapter.notifyDataSetChanged()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // 친구 목록을 가져오는 도중 에러 발생 시 처리
//            }
//        })
//    }


    private fun removeFriend(friendUid: String) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid

        val friendRef =
            myUid?.let { FirebaseDatabase.getInstance().getReference("User").child("users").child(it).child("friends") }

        friendRef?.child(friendUid)?.removeValue()
            ?.addOnSuccessListener {
                // 친구 삭제 성공
                Toast.makeText(requireContext(), "친구 삭제 성공", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            ?.addOnFailureListener { error ->
                // 친구 삭제 실패
                Toast.makeText(requireContext(), "친구 삭제 실패", Toast.LENGTH_SHORT).show()
                Log.e("RemoveFriendFragment", "Failed to remove friend: $friendUid, error: $error")
            }
    }
}

