package com.example.killersudoku.viewmodel

import com.example.killersudoku.domain.model.Game
import com.example.killersudoku.domain.model.GameStats
import com.example.killersudoku.domain.model.GridPosition
import com.example.killersudoku.domain.model.PlayerProgress
import com.example.killersudoku.domain.model.RewardResult

data class GameUiState(
    val isLoading: Boolean = true,
    val game: Game? = null,
    val stats: GameStats = GameStats(),
    val progress: PlayerProgress = PlayerProgress(),
    val completionReward: RewardResult? = null,
    val dailyCheckInReward: RewardResult? = null,
    val selectedCell: GridPosition? = null,
    val selectedCells: Set<GridPosition> = emptySet(),
    val notes: Map<GridPosition, Set<Int>> = emptyMap(),
    val cageCombinations: List<String> = emptyList(),
    val selectedCageId: Int? = null,
    val inactiveCombinations: Map<Int, Set<String>> = emptyMap(),
    val inactiveNumbers: Map<GridPosition, Set<Int>> = emptyMap(),
    val mistakes: Set<GridPosition> = emptySet(),
    val message: UiMessage? = null,
    val elapsedMillis: Long = 0L,
    val isPaused: Boolean = false,
    val showCompletionDialog: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
)
