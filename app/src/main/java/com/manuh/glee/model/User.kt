package com.manuh.glee.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey
    var id: Int,

    @ColumnInfo(name = "phone_number")
    var phone_number: String?,

    @ColumnInfo(name = "user_name")
    var user_name: String?,

    @ColumnInfo(name = "profile_photo")
    var profile_photo: String?,

    @ColumnInfo(name = "profile_banner")
    var profile_banner: String?,

    @ColumnInfo(name = "about")
    var about: String?
)