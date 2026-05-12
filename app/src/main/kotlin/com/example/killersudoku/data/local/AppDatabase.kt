package com.example.killersudoku.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.killersudoku.data.local.dao.GameDao
import com.example.killersudoku.data.local.entity.GameEntity

@Database(
    entities = [GameEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
