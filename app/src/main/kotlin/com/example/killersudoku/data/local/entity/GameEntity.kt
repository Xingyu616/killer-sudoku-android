package com.example.killersudoku.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val puzzleId: String,
    val difficulty: String,
    val initialGrid: String,
    val solutionGrid: String,
    val currentGrid: String,
    val cages: String,
    @ColumnInfo(defaultValue = "''") val notes: String = "",
    val startedAt: Long,
    val lastModified: Long,
    @ColumnInfo(defaultValue = "0") val elapsedMillis: Long = 0L,
    val timerStartedAt: Long? = startedAt,
    val pausedAt: Long? = null,
    @ColumnInfo(defaultValue = "0") val usedHint: Boolean = false,
    @ColumnInfo(defaultValue = "0") val usedSolve: Boolean = false,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
)
