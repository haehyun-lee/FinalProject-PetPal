package com.petpal.mungmate.ui.community

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.petpal.mungmate.model.Comment

class CommentViewModel : ViewModel() {
    private val _postCommentList = MutableLiveData<MutableList<Comment>>()
    val postCommentList: LiveData<MutableList<Comment>> = _postCommentList
    private val _documentChangeCount = MutableLiveData<Int>()
    var documentChangeCount: LiveData<Int> = _documentChangeCount

    fun setCommentList(commentList: MutableList<Comment>) {
        _postCommentList.value = commentList
    }
    fun deleteComment(commentToDelete: Comment) {
        val currentList = _postCommentList.value ?: mutableListOf()
        currentList.remove(commentToDelete)
        _postCommentList.value = currentList
    }

    fun updateDocumentChangeCount(newCount: Int) {
        _documentChangeCount.value = newCount
    }
}