package com.petpal.mungmate.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.petpal.mungmate.databinding.RowChatDateBinding
import com.petpal.mungmate.databinding.RowChatReceiveMessageBinding
import com.petpal.mungmate.databinding.RowChatSendMessageBinding
import com.petpal.mungmate.databinding.RowChatWalkMateAcceptBinding
import com.petpal.mungmate.databinding.RowChatWalkMateRejectBinding
import com.petpal.mungmate.databinding.RowChatWalkMateRequestBinding
import com.petpal.mungmate.model.Match
import com.petpal.mungmate.model.Message
import com.petpal.mungmate.model.MessageType
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.concurrent.fixedRateTimer

// Recyceler.ViewHolder를 상속받는 자식 클래스 ViewHolder들로 이루어진 리스트를 하나의 RecyclerView로 표시
class MessageAdapter(private val chatViewModel: ChatViewModel): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!
    private val messages = mutableListOf<Message>()

    companion object {
        const val VIEW_TYPE_SEND_TEXT = 0
        const val VIEW_TYPE_RECEIVE_TEXT = 1
        const val VIEW_TYPE_DATE = 2
        const val VIEW_TYPE_WALK_MATE_REQUEST = 3
        const val VIEW_TYPE_WALK_MATE_ACCEPT = 4
        const val VIEW_TYPE_WALK_MATE_REJECT = 5
    }

    // viewType에 따라 다른 ViewHolder 생성, 반환 타입은 부모 클래스 ViewHolder로 고정
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SEND_TEXT -> {
                val rowBinding = RowChatSendMessageBinding.inflate(LayoutInflater.from(parent.context))
                rowBinding.root.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                SendTextViewHolder(rowBinding)
            }
            VIEW_TYPE_RECEIVE_TEXT -> {
                val rowBinding = RowChatReceiveMessageBinding.inflate(LayoutInflater.from(parent.context))
                rowBinding.root.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                ReceiveTextViewHolder(rowBinding)
            }
            VIEW_TYPE_DATE -> {
                val rowBinding = RowChatDateBinding.inflate(LayoutInflater.from(parent.context))
                DateViewHolder(rowBinding)
            }
            VIEW_TYPE_WALK_MATE_REQUEST -> {
                val rowBinding = RowChatWalkMateRequestBinding.inflate(LayoutInflater.from(parent.context))
                WalkMateRequestViewHolder(rowBinding)
            }
            VIEW_TYPE_WALK_MATE_ACCEPT -> {
                val rowBinding = RowChatWalkMateAcceptBinding.inflate(LayoutInflater.from(parent.context))
                WalkMateAcceptViewHolder(rowBinding)
            }
            VIEW_TYPE_WALK_MATE_REJECT -> {
                val rowBinding = RowChatWalkMateRejectBinding.inflate(LayoutInflater.from(parent.context))
                WalkMateRejectViewHolder(rowBinding)
            }
            else -> throw IllegalArgumentException("Unkown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when(holder) {
            is SendTextViewHolder -> holder.bind(message)
            is ReceiveTextViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    // 메시지 타입에 따라 ViewHolder 타입 구분
    override fun getItemViewType(position: Int): Int {
        val message = messages[position]

        return when (message.type) {
            MessageType.TEXT.code -> {
                if (message.senderId == currentUserId) {
                    VIEW_TYPE_SEND_TEXT
                } else {
                    VIEW_TYPE_RECEIVE_TEXT
                }
            }
            MessageType.DATE.code -> VIEW_TYPE_DATE
            MessageType.WALK_MATE_REQUEST.code -> VIEW_TYPE_WALK_MATE_REQUEST
            MessageType.WALK_MATE_ACCEPT.code -> VIEW_TYPE_WALK_MATE_ACCEPT
            MessageType.WALK_MATE_REJECT.code -> VIEW_TYPE_WALK_MATE_REJECT
            else -> 0
        }
    }

    fun setMessages(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    // 보낸 메시지
    inner class SendTextViewHolder(private val rowChatSendMessageBinding: RowChatSendMessageBinding): RecyclerView.ViewHolder(rowChatSendMessageBinding.root){
        fun bind(message: Message){
            rowChatSendMessageBinding.run {
                textViewMessage.text = message.content
                val timestamp = message.timestamp?.toDate()!!
                val sdf = SimpleDateFormat("a hh시 mm분", Locale.getDefault())
                val formattedDate = sdf.format(timestamp)
                textViewTime.text = formattedDate
            }
        }
    }
    // 받은 메시지
    inner class ReceiveTextViewHolder(private val rowChatReceiveMessageBinding: RowChatReceiveMessageBinding): RecyclerView.ViewHolder(rowChatReceiveMessageBinding.root){
        fun bind(message: Message) {
            rowChatReceiveMessageBinding.run {
                textViewMessage.text = message.content
                val timestamp = message.timestamp?.toDate()!!
                val sdf = SimpleDateFormat("a hh시 mm분", Locale.getDefault())
                val formattedDate = sdf.format(timestamp)
                textViewTime.text = formattedDate
            }
        }
    }

    // 날짜
    inner class DateViewHolder(private val rowChatDateBinding: RowChatDateBinding): RecyclerView.ViewHolder(rowChatDateBinding.root) {
        fun bind(message: Message) {
            rowChatDateBinding.run {
                val date = message.timestamp?.toDate()
                val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
                textViewDate.text = sdf.format(date)
            }
        }
    }

    // 산책 요청
    inner class WalkMateRequestViewHolder(private val rowChatWalkMateRequestBinding: RowChatWalkMateRequestBinding): RecyclerView.ViewHolder(rowChatWalkMateRequestBinding.root){
        fun bind(message: Message) {
            rowChatWalkMateRequestBinding.run {
                val matchKey = message.content!!
                chatViewModel.getMatchByKey(matchKey) { document ->
                    if (document != null && document.exists()) {
                        val match = document.toObject(Match::class.java)
                        val walkTimestamp = match?.walkTimestamp

//                        textViewRequestDateTime.text =
                    } else {
                        // Document를 찾지 못하거나 오류가 난 경우
                    }
                }

                buttonAccept.setOnClickListener {
                    // TODO 수락 메시지 저장, match 상태 변경
                }
                buttonReject.setOnClickListener {
                    // TODO 거절 메시지 저장, match 상태 변경
                }
            }
        }
    }

    // 산책 수락
    inner class WalkMateAcceptViewHolder(private val rowChatWalkMateAcceptBinding: RowChatWalkMateAcceptBinding): RecyclerView.ViewHolder(rowChatWalkMateAcceptBinding.root) {
        fun bind(message: Message) {
//            rowChatWalkMateAcceptBinding.run {
//                textViewAcceptDate.text =
//                    textviewAcceptMessage.text = ""
//            }
        }
    }

    // 산책 거절
    inner class WalkMateRejectViewHolder(private val rowChatWalkMateRejectBinding: RowChatWalkMateRejectBinding): RecyclerView.ViewHolder(rowChatWalkMateRejectBinding.root){

    }
}