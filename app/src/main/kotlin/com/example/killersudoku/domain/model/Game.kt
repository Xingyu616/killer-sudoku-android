package com.example.killersudoku.domain.model

data class Game(
    val id: Long = 0,
    val puzzle: Puzzle,
    val currentGrid: Grid,
    val notes: Map<GridPosition, Set<Int>> = emptyMap(),
    val startedAt: Long,
    val lastModified: Long,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
) {
    fun withCell(position: GridPosition, value: Int): Game =
        copy(
            currentGrid = currentGrid.withValue(position, value),
            notes = if (value == 0) notes else notes - position,
            lastModified = System.currentTimeMillis(),
        )

    fun withNotes(notes: Map<GridPosition, Set<Int>>): Game =
        copy(
            notes = notes.filterValues { it.isNotEmpty() },
            lastModified = System.currentTimeMillis(),
        )

    fun completedIfSolved(): Game {
        val solved = currentGrid == puzzle.solutionGrid
        return if (solved) {
            copy(
                isCompleted = true,
                completedAt = System.currentTimeMillis(),
                lastModified = System.currentTimeMillis(),
            )
        } else {
            this
        }
    }
}
