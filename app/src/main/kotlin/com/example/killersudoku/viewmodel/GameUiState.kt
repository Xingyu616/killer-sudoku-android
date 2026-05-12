package com.example.killersudoku.viewmodel

import com.example.killersudoku.domain.model.Game
import com.example.killersudoku.domain.model.GridPosition

data class GameUiState(
    val isLoading: Boolean = true,
    val game: Game? = null,
    val selectedCell: GridPosition? = null,
    val noteMode: Boolean = false,
    val notes: Map<GridPosition, Set<Int>> = emptyMap(),
    val mistakes: Set<GridPosition> = emptySet(),
    val message: String? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
)
