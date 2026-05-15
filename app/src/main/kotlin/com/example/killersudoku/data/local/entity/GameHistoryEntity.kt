package com.example.killersudoku.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_history")
data class GameHistoryEntity(
    @PrimaryKey val gameId: Long,
    val difficulty: String,
    val startedAt: Long,
    val completedAt: Long,
    val elapsedMillis: Long,
    val usedHint: Boolean,
    val usedSolve: Boolean,
)
