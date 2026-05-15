package com.example.killersudoku.domain.usecase

import com.example.killersudoku.domain.model.Game
import javax.inject.Inject

class SolvePuzzleUseCase @Inject constructor(
    private val solver: KillerSudokuSolver,
) {
    operator fun invoke(game: Game): Game {
        val solution = solver.solve(game.puzzle.initialGrid, game.puzzle.cages) ?: game.puzzle.solutionGrid
        val now = System.currentTimeMillis()
        return game.copy(
            currentGrid = solution,
            notes = emptyMap(),
            elapsedMillis = game.currentElapsedMillis(now),
            timerStartedAt = null,
            pausedAt = null,
            usedSolve = true,
            isCompleted = true,
            completedAt = now,
            lastModified = now,
        )
    }
}
