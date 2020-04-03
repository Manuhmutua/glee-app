package com.manuh.glee.source

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.manuh.glee.dao.UserDao
import com.manuh.glee.model.User

@androidx.room.Database(entities = [User::class], version = 1, exportSchema = false)
abstract class Database : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {

        @Volatile
        private var INSTANCE: Database? = null

        fun getDatabase(context: Context): Database? {
            if (INSTANCE == null) {
                synchronized(Database::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            Database::class.java, "glee_database"
                        )
                            .build()
                    }
                }
            }
            return INSTANCE
        }
    }
}