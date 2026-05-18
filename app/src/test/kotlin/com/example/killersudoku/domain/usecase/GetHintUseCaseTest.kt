package com.example.killersudoku.domain.usecase

import com.example.killersudoku.domain.model.Cage
import com.example.killersudoku.domain.model.Difficulty
import com.example.killersudoku.domain.model.Game
import com.example.killersudoku.domain.model.Grid
import com.example.killersudoku.domain.model.GridPosition
import com.example.killersudoku.domain.model.Puzzle
import com.example.killersudoku.domain.model.emptyGrid
import com.example.killersudoku.domain.model.withValue
import org.junit.Assert.assertEquals
import org.junit.Test

class GetHintUseCaseTest {
    private val getHint = GetHintUseCase()

    @Test
    fun prefersMostConstrainedCellWhenNoCellIsSelected() {
        val solution = solvedGrid()
        val grid = (0..7).fold(emptyGrid()) { current, col ->
            current.withValue(GridPosition(0, col), solution[0][col])
        }
        val game = sampleGame(solution = solution, currentGrid = grid)

        val hint = getHint(game, preferredPosition = null)

        assertEquals(GridPosition(0, 8), hint?.position)
        assertEquals(9, hint?.answer)
    }

    private fun sampleGame(
        solution: Grid,
        currentGrid: Grid,
    ): Game {
        val cages = solution.flatMapIndexed { row, values ->
            values.mapIndexed { col, value ->
                val position = GridPosition(row, col)
                Cage(
                    id = row * 9 + col,
                    cells = listOf(position),
                    targetSum = value,
                )
            }
        }
        return Game(
            puzzle = Puzzle(
                id = "test",
                difficulty = Difficulty.LEVEL_2,
                initialGrid = emptyGrid(),
                solutionGrid = solution,
                cages = cages,
                createdAt = 0L,
            ),
            currentGrid = currentGrid,
            startedAt = 0L,
            lastModified = 0L,
        )
    }

    private fun solvedGrid(): Grid =
        List(9) { row -> List(9) { col -> ((row * 3 + row / 3 + col) % 9) + 1 } }
}
