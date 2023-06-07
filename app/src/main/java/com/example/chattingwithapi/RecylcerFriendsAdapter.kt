package com.example.chattingwithapi

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingwithapi.databinding.ListPersonItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RecyclerFriendsAdapter(private val fragment: RemoveFriendFragment) : RecyclerView.Adapter<RecyclerFriendsAdapter.ViewHolder>() {

    private val friends: ArrayList<User> = arrayListOf()
    lateinit var currentUser: User

    init {
        setupFriendList()
    }

    private fun setupFriendList() {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val usersRef = FirebaseDatabase.getInstance().getReference("User").child("users")

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updatedFriends = ArrayList<User>()

                val friendsRef = usersRef.child(myUid).child("friends")
                friendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(friendsSnapshot: DataSnapshot) {
                        for (friendSnapshot in friendsSnapshot.children) {
                            val friendUid = friendSnapshot.key
                            val friendUser = friendUid?.let { snapshot.child(it).getValue(User::class.java) }
                            friendUser?.let {
                                updatedFriends.add(it)
                            }
                        }

                        friends.clear()
                        friends.addAll(updatedFriends)

                        notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // 오류 처리
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                // 오류 처리
            }
        })
    }



    // Rest of the adapter code...


    fun removeFriend(user: User) {
        friends.remove(user)
        notifyDataSetChanged()
        // 친구 제거 시 RemoveFriendFragment에서 처리할 로직 추가 가능
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_person_item, parent, false)
        return ViewHolder(ListPersonItemBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(friends[position])
    }

    override fun getItemCount(): Int {
        return friends.size
    }

    inner class ViewHolder(private val binding: ListPersonItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.txtName.text = user.name
            binding.txtEmail.text = user.email

            binding.background.setOnClickListener {
                removeFriend(user)
                deleteFriendFromDatabase(user)
            }
        }

        private fun deleteFriendFromDatabase(user: User) {
            val myUid = FirebaseAuth.getInstance().currentUser?.uid
            val friendUid = user.uid

            val friendRef = myUid?.let {
                FirebaseDatabase.getInstance().getReference("User")
                    .child("users")
                    .child(it)
                    .child("friends")
                    .child(friendUid!!)
            }

            friendRef?.removeValue()
                ?.addOnSuccessListener {
                    // 친구 삭제 성공
                    // 여기서 필요한 UI 업데이트를 수행하거나 사용자에게 알림을 표시할 수 있습니다.
                }
                ?.addOnFailureListener { error ->
                    // 친구 삭제 실패
                    // 실패 처리에 대한 로직을 추가할 수 있습니다.
                }
        }
    }
}



