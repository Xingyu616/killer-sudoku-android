package com.example.killersudoku.data.di

import android.content.Context
import androidx.room.Room
import com.example.killersudoku.data.local.AppDatabase
import com.example.killersudoku.data.local.dao.GameDao
import com.example.killersudoku.data.repository.GameRepositoryImpl
import com.example.killersudoku.domain.repository.GameRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindGameRepository(impl: GameRepositoryImpl): GameRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "killer_sudoku.db",
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideGameDao(database: AppDatabase): GameDao = database.gameDao()
}
