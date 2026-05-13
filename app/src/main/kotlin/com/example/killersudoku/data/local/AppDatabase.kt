package com.example.killersudoku.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.killersudoku.data.local.dao.GameDao
import com.example.killersudoku.data.local.entity.GameEntity

@Database(
    entities = [GameEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE games ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
