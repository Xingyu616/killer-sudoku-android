package com.example.killersudoku.domain.model

data class Game(
    val id: Long = 0,
    val puzzle: Puzzle,
    val currentGrid: Grid,
    val startedAt: Long,
    val lastModified: Long,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
) {
    fun withCell(position: GridPosition, value: Int): Game =
        copy(
            currentGrid = currentGrid.withValue(position, value),
            lastModified = System.currentTimeMillis(),
        )

    fun completedIfSolved(): Game {
        val solved = currentGrid == puzzle.solutionGrid
        return if (solved) {
            copy(isCompleted = true, completedAt = System.currentTimeMillis())
        } else {
            this
        }
    }
}
