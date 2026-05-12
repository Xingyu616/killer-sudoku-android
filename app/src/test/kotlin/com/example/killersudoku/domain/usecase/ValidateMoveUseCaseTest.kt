package com.example.killersudoku.domain.usecase

import com.example.killersudoku.domain.model.Cage
import com.example.killersudoku.domain.model.Difficulty
import com.example.killersudoku.domain.model.Game
import com.example.killersudoku.domain.model.GridPosition
import com.example.killersudoku.domain.model.Puzzle
import com.example.killersudoku.domain.model.emptyGrid
import com.example.killersudoku.domain.model.withValue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateMoveUseCaseTest {
    private val validateMove = ValidateMoveUseCase()

    @Test
    fun rejectsDuplicateInRow() {
        val game = sampleGame().copy(
            currentGrid = emptyGrid().withValue(GridPosition(0, 1), 1),
        )

        val result = validateMove(game, GridPosition(0, 0), 1)

        assertFalse(result.isValid)
    }

    @Test
    fun acceptsMoveThatFitsAllCurrentConstraints() {
        val result = validateMove(sampleGame(), GridPosition(0, 0), 1)

        assertTrue(result.isValid)
    }

    private fun sampleGame(): Game {
        val solution = List(9) { row ->
            List(9) { col -> ((row * 3 + row / 3 + col) % 9) + 1 }
        }
        val cages = (0..8).flatMap { row ->
            (0..8).map { col ->
                val position = GridPosition(row, col)
                Cage(
                    id = row * 9 + col,
                    cells = listOf(position),
                    targetSum = solution[row][col],
                )
            }
        }
        val puzzle = Puzzle(
            id = "test",
            difficulty = Difficulty.EASY,
            initialGrid = emptyGrid(),
            solutionGrid = solution,
            cages = cages,
            createdAt = 0L,
        )
        return Game(
            puzzle = puzzle,
            currentGrid = emptyGrid(),
            startedAt = 0L,
            lastModified = 0L,
        )
    }
}
