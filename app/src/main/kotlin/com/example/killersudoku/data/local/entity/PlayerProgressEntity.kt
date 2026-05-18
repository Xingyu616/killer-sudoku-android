package com.example.killersudoku.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_progress")
data class PlayerProgressEntity(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 0,
    val lastCheckInDate: String? = null,
    val checkInStreak: Int = 0,
    val lastFirstWinDate: String? = null,
)
