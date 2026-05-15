package com.example.killersudoku.domain.model

data class Game(
    val id: Long = 0,
    val puzzle: Puzzle,
    val currentGrid: Grid,
    val notes: Map<GridPosition, Set<Int>> = emptyMap(),
    val startedAt: Long,
    val lastModified: Long,
    val elapsedMillis: Long = 0L,
    val timerStartedAt: Long? = startedAt,
    val pausedAt: Long? = null,
    val usedHint: Boolean = false,
    val usedSolve: Boolean = false,
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

    fun currentElapsedMillis(now: Long = System.currentTimeMillis()): Long =
        if (isCompleted || pausedAt != null || timerStartedAt == null) {
            elapsedMillis
        } else {
            elapsedMillis + (now - timerStartedAt).coerceAtLeast(0L)
        }

    fun pause(now: Long = System.currentTimeMillis()): Game =
        if (isCompleted || pausedAt != null) {
            this
        } else {
            copy(
                elapsedMillis = currentElapsedMillis(now),
                timerStartedAt = null,
                pausedAt = now,
                lastModified = now,
            )
        }

    fun resume(now: Long = System.currentTimeMillis()): Game =
        if (isCompleted || pausedAt == null) {
            this
        } else {
            copy(
                timerStartedAt = now,
                pausedAt = null,
                lastModified = now,
            )
        }

    fun completedIfSolved(now: Long = System.currentTimeMillis()): Game {
        val solved = currentGrid == puzzle.solutionGrid
        return if (solved) {
            copy(
                elapsedMillis = currentElapsedMillis(now),
                timerStartedAt = null,
                pausedAt = null,
                isCompleted = true,
                completedAt = now,
                lastModified = now,
            )
        } else {
            this
        }
    }
}
