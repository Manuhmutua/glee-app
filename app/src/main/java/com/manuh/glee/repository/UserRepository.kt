package com.manuh.glee.repository

import android.app.Application
import com.manuh.glee.dao.UserDao
import com.manuh.glee.model.User
import com.manuh.glee.source.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class UserRepository(application: Application) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private var userDao: UserDao?

    init {
        val db = Database.getDatabase(application)
        userDao = db?.userDao()
    }

    fun getUser() = userDao!!.getUser()

    fun setUser(user: User) {
        launch { setUserBG(user) }
    }

    private suspend fun setUserBG(user: User) {
        withContext(Dispatchers.IO) {
            userDao?.setUser(user)
        }
    }

}