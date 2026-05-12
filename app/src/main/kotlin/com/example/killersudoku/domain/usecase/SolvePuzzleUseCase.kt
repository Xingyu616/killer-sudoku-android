package com.example.killersudoku.domain.usecase

import com.example.killersudoku.domain.model.Game
import javax.inject.Inject

class SolvePuzzleUseCase @Inject constructor() {
    operator fun invoke(game: Game): Game =
        game.copy(
            currentGrid = game.puzzle.solutionGrid,
            isCompleted = true,
            completedAt = System.currentTimeMillis(),
            lastModified = System.currentTimeMillis(),
        )
}
