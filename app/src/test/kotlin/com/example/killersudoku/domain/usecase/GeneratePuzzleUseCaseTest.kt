package com.example.killersudoku.domain.usecase

import com.example.killersudoku.domain.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratePuzzleUseCaseTest {
    private val generatePuzzle = GeneratePuzzleUseCase()

    @Test
    fun generatedPuzzleHasValidShapeAndCages() {
        val puzzle = generatePuzzle(Difficulty.MEDIUM)
        val cageCells = puzzle.cages.flatMap { it.cells }

        assertEquals(9, puzzle.solutionGrid.size)
        assertTrue(puzzle.solutionGrid.all { it.size == 9 })
        assertEquals(81, cageCells.size)
        assertEquals(81, cageCells.toSet().size)

        puzzle.cages.forEach { cage ->
            val sum = cage.cells.sumOf { puzzle.solutionGrid[it.row][it.col] }
            assertEquals(sum, cage.targetSum)
        }
    }
}
