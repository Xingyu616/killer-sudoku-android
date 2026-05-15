package com.example.killersudoku.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameTimerTest {
    @Test
    fun pauseFreezesElapsedTime() {
        val game = sampleGame(timerStartedAt = 1_000L)

        val paused = game.pause(now = 6_000L)

        assertEquals(5_000L, paused.elapsedMillis)
        assertEquals(6_000L, paused.pausedAt)
        assertNull(paused.timerStartedAt)
        assertEquals(5_000L, paused.currentElapsedMillis(now = 9_000L))
    }

    @Test
    fun completionFreezesElapsedTime() {
        val solution = solvedGrid()
        val game = sampleGame(
            currentGrid = solution,
            solutionGrid = solution,
            timerStartedAt = 2_000L,
        )

        val completed = game.completedIfSolved(now = 8_000L)

        assertEquals(6_000L, completed.elapsedMillis)
        assertEquals(8_000L, completed.completedAt)
        assertNull(completed.timerStartedAt)
        assertNull(completed.pausedAt)
    }

    private fun sampleGame(
        currentGrid: Grid = emptyGrid(),
        solutionGrid: Grid = solvedGrid(),
        timerStartedAt: Long,
    ): Game =
        Game(
            puzzle = Puzzle(
                id = "test",
                difficulty = Difficulty.LEVEL_2,
                initialGrid = emptyGrid(),
                solutionGrid = solutionGrid,
                cages = emptyList(),
                createdAt = 0L,
            ),
            currentGrid = currentGrid,
            startedAt = 0L,
            lastModified = 0L,
            elapsedMillis = 0L,
            timerStartedAt = timerStartedAt,
        )

    private fun solvedGrid(): Grid =
        List(9) { row -> List(9) { col -> ((row * 3 + row / 3 + col) % 9) + 1 } }
}
