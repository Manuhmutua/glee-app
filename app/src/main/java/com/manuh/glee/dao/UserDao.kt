package com.manuh.glee.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.manuh.glee.model.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setUser(user: User)

    @Query("SELECT * from user_table WHERE id=0")
    fun getUser(): LiveData<User>
}