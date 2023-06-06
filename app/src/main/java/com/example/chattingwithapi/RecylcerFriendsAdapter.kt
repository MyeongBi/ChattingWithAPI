package com.example.chattingwithapi

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingwithapi.databinding.ListPersonItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RecyclerFriendsAdapter(private val context: Context, private val onRemoveClickListener: (User) -> Unit) : RecyclerView.Adapter<RecyclerFriendsAdapter.ViewHolder>() {

    private val friends: ArrayList<User> = ArrayList()
    lateinit var currentUser: User

    init {
        setupFriendList()
    }

    private fun setupFriendList() { // 친구 목록 불러오기
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 사용자 아이디
        FirebaseDatabase.getInstance().getReference("User")
            .child("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    friends.clear()
                    for (data in snapshot.children) {
                        val item = data.getValue(User::class.java)
                        if (item?.uid == myUid) {
                            currentUser = item!! // 전체 사용자 목록에서 현재 사용자는 제외
                            continue
                        }
                        FirebaseDatabase.getInstance().getReference("User")
                            .child("friends")
                            .child(item?.uid!!)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(friendsSnapshot: DataSnapshot) {
                                    val friendList =
                                        friendsSnapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                                    if (friendList?.contains(myUid) == true) {
                                        friends.add(data.getValue(User::class.java)!!) // 친구 목록에 추가
                                    }
                                    notifyDataSetChanged() // 화면 업데이트
                                }

                                override fun onCancelled(error: DatabaseError) {
                                }
                            })
                    }
                }
            })
    }




    fun removeFriend(user: User) {
        friends.remove(user)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_person_item, parent, false)
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


