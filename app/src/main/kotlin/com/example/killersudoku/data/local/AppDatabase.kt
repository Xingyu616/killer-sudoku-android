package com.example.killersudoku.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.killersudoku.data.local.dao.GameDao
import com.example.killersudoku.data.local.entity.GameEntity
import com.example.killersudoku.data.local.entity.GameHistoryEntity
import com.example.killersudoku.data.local.entity.PlayerProgressEntity

@Database(
    entities = [GameEntity::class, GameHistoryEntity::class, PlayerProgressEntity::class],
    version = 4,
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE games ADD COLUMN elapsedMillis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE games ADD COLUMN timerStartedAt INTEGER")
                db.execSQL("ALTER TABLE games ADD COLUMN pausedAt INTEGER")
                db.execSQL("ALTER TABLE games ADD COLUMN usedHint INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE games ADD COLUMN usedSolve INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE games SET timerStartedAt = lastModified WHERE isCompleted = 0")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS game_history (
                        gameId INTEGER NOT NULL,
                        difficulty TEXT NOT NULL,
                        startedAt INTEGER NOT NULL,
                        completedAt INTEGER NOT NULL,
                        elapsedMillis INTEGER NOT NULL,
                        usedHint INTEGER NOT NULL,
                        usedSolve INTEGER NOT NULL,
                        PRIMARY KEY(gameId)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO game_history (
                        gameId,
                        difficulty,
                        startedAt,
                        completedAt,
                        elapsedMillis,
                        usedHint,
                        usedSolve
                    )
                    SELECT
                        id,
                        difficulty,
                        startedAt,
                        completedAt,
                        0,
                        0,
                        0
                    FROM games
                    WHERE isCompleted = 1 AND completedAt IS NOT NULL
                    """.trimIndent(),
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE game_history ADD COLUMN rewardCoins INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE game_history ADD COLUMN rewardTier TEXT NOT NULL DEFAULT 'NONE'")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS player_progress (
                        id INTEGER NOT NULL,
                        coins INTEGER NOT NULL,
                        lastCheckInDate TEXT,
                        checkInStreak INTEGER NOT NULL,
                        lastFirstWinDate TEXT,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO player_progress (
                        id,
                        coins,
                        lastCheckInDate,
                        checkInStreak,
                        lastFirstWinDate
                    )
                    VALUES (1, 0, NULL, 0, NULL)
                    """.trimIndent(),
                )
            }
        }
    }
}
