package com.geminiai.studywithai.screens.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.geminiai.studywithai.models.Book
import com.geminiai.studywithai.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(private val bookRepository: BookRepository) : ViewModel() {
    val bookLiveData: LiveData<Book> = bookRepository.getBook()
}