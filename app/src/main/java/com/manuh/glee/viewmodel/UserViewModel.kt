package com.manuh.glee.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.manuh.glee.model.User
import com.manuh.glee.repository.UserRepository

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private var repository: UserRepository = UserRepository(application)

    fun getUser() = repository.getUser()

    fun setUser(user: User) {
        repository.setUser(user)
    }
}