package com.example.killersudoku.data.mapper

import com.example.killersudoku.data.local.entity.GameEntity
import com.example.killersudoku.domain.model.Cage
import com.example.killersudoku.domain.model.Difficulty
import com.example.killersudoku.domain.model.Game
import com.example.killersudoku.domain.model.GridPosition
import com.example.killersudoku.domain.model.Puzzle
import com.example.killersudoku.domain.model.emptyGrid
import org.junit.Assert.assertEquals
import org.junit.Test

class GameMapperTest {
    @Test
    fun preservesNotesWhenMappingThroughEntity() {
        val position = GridPosition(2, 3)
        val game = Game(
            id = 7L,
            puzzle = Puzzle(
                id = "test",
                difficulty = Difficulty.LEVEL_2,
                initialGrid = emptyGrid(),
                solutionGrid = emptyGrid(),
                cages = listOf(Cage(id = 1, cells = listOf(position), targetSum = 4)),
                createdAt = 1L,
            ),
            currentGrid = emptyGrid(),
            notes = mapOf(position to setOf(1, 4, 7)),
            startedAt = 1L,
            lastModified = 2L,
            elapsedMillis = 3_000L,
            timerStartedAt = 4L,
            pausedAt = 5L,
            usedHint = true,
            usedSolve = false,
        )

        val restored = game.toEntity().toDomain()

        assertEquals(game.notes, restored.notes)
        assertEquals(game.elapsedMillis, restored.elapsedMillis)
        assertEquals(game.timerStartedAt, restored.timerStartedAt)
        assertEquals(game.pausedAt, restored.pausedAt)
        assertEquals(game.usedHint, restored.usedHint)
        assertEquals(game.usedSolve, restored.usedSolve)
    }

    @Test
    fun mapsCompletedGameToHistoryEntity() {
        val game = Game(
            id = 7L,
            puzzle = Puzzle(
                id = "test",
                difficulty = Difficulty.LEVEL_8,
                initialGrid = emptyGrid(),
                solutionGrid = emptyGrid(),
                cages = emptyList(),
                createdAt = 1L,
            ),
            currentGrid = emptyGrid(),
            startedAt = 1L,
            lastModified = 3L,
            elapsedMillis = 12_000L,
            usedHint = true,
            usedSolve = true,
            isCompleted = true,
            completedAt = 3L,
        )

        val history = game.toHistoryEntity()!!.toDomain()

        assertEquals(game.id, history.gameId)
        assertEquals(game.puzzle.difficulty, history.difficulty)
        assertEquals(game.startedAt, history.startedAt)
        assertEquals(game.completedAt, history.completedAt)
        assertEquals(game.elapsedMillis, history.elapsedMillis)
        assertEquals(game.usedHint, history.usedHint)
        assertEquals(game.usedSolve, history.usedSolve)
    }

    @Test
    fun mapsLegacyDifficultyNames() {
        val entity = GameEntity(
            id = 1L,
            puzzleId = "legacy",
            difficulty = "HARD",
            initialGrid = emptyGrid().encodeGrid(),
            solutionGrid = emptyGrid().encodeGrid(),
            currentGrid = emptyGrid().encodeGrid(),
            cages = "",
            startedAt = 1L,
            lastModified = 1L,
        )

        assertEquals(Difficulty.LEVEL_8, entity.toDomain().puzzle.difficulty)
    }
}
