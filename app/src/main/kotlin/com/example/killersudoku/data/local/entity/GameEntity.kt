package com.example.killersudoku.data.local.entity

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
    val startedAt: Long,
    val lastModified: Long,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
)
